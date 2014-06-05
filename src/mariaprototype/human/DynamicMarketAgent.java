package mariaprototype.human;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import mariaprototype.MariaPriorities;
import mariaprototype.environmental.LandUse;
import mariaprototype.human.messaging.EnvelopePool;
import mariaprototype.human.messaging.MarketPrices;
import mariaprototype.human.messaging.MessageEnvelope;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunState;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.util.ContextUtils;

public class DynamicMarketAgent extends MarketAgent {
	private EnvelopePool marketEnvelopePool;
	private MarketPrices marketPrices;
	
	private Map<LandUse, ArrayList<Double>> prices = new HashMap<LandUse, ArrayList<Double>>();
	
	public DynamicMarketAgent(Map<LandUse, InputStream> priceStreams) {
		this(priceStreams, new HashMap<LandUse, Double>());
	}
	
	public DynamicMarketAgent(Map<LandUse, InputStream> priceStreams, Map<LandUse, Double> multipliers) {
		marketEnvelopePool = new EnvelopePool();
		marketPrices = new MarketPrices();

		// parse input streams
		// format (without square brackets): [name][whitespace][price],...
		for (Map.Entry<LandUse, InputStream> e : priceStreams.entrySet()) {
			ArrayList<Double> cPrices = new ArrayList<Double>(60);
			Double multiplier = multipliers.get(e.getKey());
			if (multiplier == null)
				multiplier = 1.0;
			
			try {
				Reader r = new BufferedReader(new InputStreamReader(e.getValue()));
				StreamTokenizer st = new StreamTokenizer(r);
				
				// initialize parser
				st.parseNumbers();
				st.eolIsSignificant(false);
				st.whitespaceChars(',', ',');
				
				while (true) {
					st.nextToken();
					if (st.ttype == StreamTokenizer.TT_EOF)
						break;
					else if (st.ttype == StreamTokenizer.TT_NUMBER) {
						cPrices.add(multiplier * st.nval);
					}
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			} finally {
				try {
					e.getValue().close();
				} catch (IOException e1) {}
			}
			prices.put(e.getKey(), cPrices);
		}
		
	}

	@ScheduledMethod(start = 1, interval = 1, priority = MariaPriorities.MESSAGE_PASSING_1)
	@SuppressWarnings("unchecked")
	public void gaugePrices() {
		// broadcast this year's market prices to the world

		// load prices from file
		int tick = (int) RunState.getInstance().getScheduleRegistry().getModelSchedule().getTickCount();
		for (Map.Entry<LandUse, ArrayList<Double>> e : prices.entrySet()) {
			marketPrices.setPrice(e.getKey(), e.getValue().get(tick));
		}

		// set policy (read from file)
		MessageEnvelope messageEnvelope = marketEnvelopePool.getBroadcastEnvelope(this, marketPrices);

		// send policy
		Context<HouseholdAgent> context = ContextUtils.getContext(this);
		Iterator<HouseholdAgent> iter = context.getObjects(HouseholdAgent.class).iterator();

		while (iter.hasNext()) {
			HouseholdAgent h = iter.next();
			messageEnvelope.send(h);
		}

		messageEnvelope.discard();
	}

}
