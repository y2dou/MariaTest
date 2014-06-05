package mariaprototype.human;

import java.util.Iterator;

import mariaprototype.MariaPriorities;
import mariaprototype.environmental.LandUse;
import mariaprototype.human.messaging.EnvelopePool;
import mariaprototype.human.messaging.MarketPrices;
import mariaprototype.human.messaging.MessageEnvelope;
import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.util.ContextUtils;

public class StaticMarketAgent extends MarketAgent {
	private EnvelopePool marketEnvelopePool;
	private MarketPrices marketPrices;
	
	public StaticMarketAgent() {
		marketEnvelopePool = new EnvelopePool();
		marketPrices = new MarketPrices();
	}
	
	public void setPrice(LandUse landUse, double price) {		
		marketPrices.setPrice(landUse, price);
	}
	
	@ScheduledMethod(start = 1, interval = 1, priority = MariaPriorities.MESSAGE_PASSING_1)
	@SuppressWarnings("unchecked")
	public void gaugePrices() {
		// broadcast this year's market prices to the world

		// set policy (read from file)
		MessageEnvelope messageEnvelope = marketEnvelopePool.getBroadcastEnvelope(this, marketPrices);

		// send policy
		Context<HouseholdAgent> context = ContextUtils.getContext(this);
		Iterator<HouseholdAgent> iter = context
				.getObjects(HouseholdAgent.class).iterator();

		while (iter.hasNext()) {
			HouseholdAgent h = iter.next();
			messageEnvelope.send(h);
		}

		messageEnvelope.discard();
	}
}
