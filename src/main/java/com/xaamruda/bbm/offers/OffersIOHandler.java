package com.xaamruda.bbm.offers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.xaamruda.bbm.commons.json.JsonUtils;
import com.xaamruda.bbm.offers.dbaccess.services.IOfferService;
import com.xaamruda.bbm.offers.model.Offer;
import com.xaamruda.bbm.offers.search.engine.QueryEngine;
import com.xaamruda.bbm.offers.search.engine.Filters;

@Component
public class OffersIOHandler {
	
	@Autowired
	private IOfferService offerService;

	@Autowired
	private com.xaamruda.bbm.billing.BillingIOHandler calculatorHandler;
	
	public OffersIOHandler() {
	}
	
	public List<Offer> getOffers() {
		return offerService.getAvailableOffers();
	}

	public boolean postNewOffer(String jsonObject) {
		return offerService.createNewOffer(jsonObject);
	}

	// TODO add filterChecker to add the "status.Available" filter ?
	// TODO add check on offer if lenght == 0
	// TODO
	public List<Offer> retrieveOffers(String filters, String workData) {
		List<Offer> offers = offerService
				.getAvailableOffers(QueryEngine.buildMongoQuery(JsonUtils.getFromJson(filters, Filters.class)));
		
		for (Offer offer : offers) {
			calculatorHandler.doWork(workData, offer);
		}
		
		return offers;
	}
}