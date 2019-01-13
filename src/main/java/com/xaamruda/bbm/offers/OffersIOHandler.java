package com.xaamruda.bbm.offers;

import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Component;
import com.google.gson.JsonObject;
import com.xaamruda.bbm.billing.BillingIOHandler;
import com.xaamruda.bbm.commons.json.JsonUtils;
import com.xaamruda.bbm.commons.logging.BBMLogger;
import com.xaamruda.bbm.offers.dbaccess.service.IOfferService;
import com.xaamruda.bbm.offers.dbaccess.service.IOffersTransactionService;
import com.xaamruda.bbm.offers.model.OfferStatus;
import com.xaamruda.bbm.offers.model.OffersTransaction;
import com.xaamruda.bbm.offers.model.PostedOffer;
import com.xaamruda.bbm.offers.utils.Range;
import com.xaamruda.bbm.roads.RoadsIOHandler;
import com.xaamruda.bbm.users.UsersIOHandler;
import com.xaamruda.bbm.offers.search.engine.Filters;
import com.xaamruda.bbm.offers.search.engine.QueryEngine;

@Component
public class OffersIOHandler {

	@Autowired
	private IOfferService offerService;

	@Autowired
	private IOffersTransactionService offerTransactionService;

	@Autowired
	private BillingIOHandler calculatorHandler;

	@Autowired
	private UsersIOHandler usersHandler;

	@Autowired
	private RoadsIOHandler pathHandler;

	public OffersIOHandler() {
	}

	public List<PostedOffer> getOffers() {
		try {
			return offerService.getAvailableOffers();
		}catch(Exception ex) {
			//TODO journalisation
			System.out.println(ex.toString());
			return null;
		}
	}

	public String postNewOffer(String jsonObject) {
		PostedOffer offer = JsonUtils.getFromJson(jsonObject, PostedOffer.class);
		//		offer.setOfferID(offer.getOwnerID() + new Date().getTime() + "_" + offer.getPrice());

		int distance = pathHandler.getPathDistances(offer.getStartCity(), offer.getEndCity());
		List<PostedOffer> offers = null;
		try {
			offers = offerService.getAvailableOffers(QueryEngine.buildMySqlQuery(distance));
		}catch(Exception ex) {
			//TODO journalisation
			System.out.println(ex.toString());
		}

		Range range = calculatorHandler.checkPrice(offers, distance);
		BBMLogger.infoln("Authorized price range is [" + range.getInfValue() + " : " + range.getSupValue() + "]");

		if ((offer.getPrice() < range.getSupValue() && offer.getPrice() > range.getInfValue())) {
			BBMLogger.infoln("Setting offer...");
			offer.setDistance(distance);
			BBMLogger.infoln("Offer distance set.");
			offer.setStatus(OfferStatus.POSTED);
			BBMLogger.infoln("Offer status set to " + OfferStatus.POSTED);
			offerService.createNewOffer(offer);
			BBMLogger.infoln("Offer created. Content:");
			String json = JsonUtils.toJson(offer);

			logJson(offer);
			return json;
		}
		return "Incorrect price ! For this distance ("
		+ distance + ") the authorized points amount is within [" + range.getInfValue() + " : "
		+ range.getSupValue() + "].\n";
	} 

	public List<PostedOffer> retrieveOffers(String filters, String workData) {

		Filters filtersObject = JsonUtils.getFromJson(filters, Filters.class);
		List<PostedOffer> offers = null;
		try {
			offers = offerService.getAvailableOffers(QueryEngine.buildMySqlQuery(filtersObject));
		}catch(Exception ex) {
			//TODO journalisation 
			System.out.println(ex.toString());
		}
		BBMLogger.infoln("Calculating offers prices...");
		for (PostedOffer offer : offers) {
			offer.setPrice(offer.getPrice() + calculatorHandler.calcul_without_offer(workData, offer.getDistance()));
		}

		BBMLogger.infoln(offers.size() + " offers processed.");
		BBMLogger.infoln("Filtering offers by prices...");
		return offers.stream().filter(offer -> offer.getPrice() < filtersObject.getMaxPrice())
				.collect(Collectors.toList());
	} 


	public String validatePrice(String filters, String workData) {
		Filters fil = JsonUtils.getFromJson(filters, Filters.class);

		BBMLogger.infoln("Computing path distance...");

		int distance = pathHandler.getPathDistances(fil.startAddress, fil.endAddress);
		List<PostedOffer> offers = null;

		try {
			offers = offerService.getAvailableOffers(QueryEngine.buildMySqlQuery(distance));
		} catch(Exception ex) {
			//TODO journalisation
			System.out.println(ex);
		}

		BBMLogger.infoln("Checking offers price...");
		Range range = calculatorHandler.checkPrice(offers, distance);

		return (fil.maxPrice < range.getSupValue() && fil.maxPrice > range.getInfValue()) ? "Correct price ! For the distance the authorized amount is [" + range.getInfValue() + " : "
		+ range.getSupValue() + "]\n" : "Incorrect price ! For this distance ("
		+ distance + ") the authorized points amount is within [" + range.getInfValue() + " : "
		+ range.getSupValue() + "]\n";
	} 

	// TODO rename
	// this is the method to ask the offer to get accepted by ALICE
	public String askValidate(String workData) {

		JsonObject json = JsonUtils.getFromJson(workData);

		String offerID = json.get("offerID").getAsString();
		String buyerID = json.get("buyerID").getAsString();

		List<PostedOffer> offers = null;

		try {
			offers = offerService.getOfferByID(offerID);
		} catch(Exception ex) {
			//TODO journalisation
			System.out.println(ex.toString());
		}	

		if (!offers.isEmpty() && offers.get(0).getStatus() == OfferStatus.POSTED) {
			BBMLogger.infoln("Computing pricing...");

			PostedOffer offer = offers.get(0);


			int newPrice = offer.getPrice() + calculatorHandler.calcul_without_offer(workData, offer.getDistance());
			usersHandler.sendMail(offer.getOwnerID(), newPrice, buyerID);

			OffersTransaction offerTransaction = new OffersTransaction();
			BBMLogger.infoln("Creating offer transaction...");

			//Init offer transaction
			offerTransaction.setBuyerID(buyerID);
			offerTransaction.setFinalPrice(newPrice);
			offerTransaction.setStatus(OfferStatus.AWAITING_CONFIRMATION);
			offerTransaction.setOfferID(offerID);
			offerTransaction.setOwnerID(offer.getOwnerID());
			offerTransaction.setWeigth(json.get("weight").getAsInt());
			offerTransaction.setVolume(json.get("volume").getAsInt());
			offerTransaction.setDateBeforeOrder(json.get("date").getAsInt());
			offerTransaction.setAskforConfirmationDate(new Date().toString());
			offer.setStatus(OfferStatus.AWAITING_CONFIRMATION);

			//Store offer in database
			try {
				offerService.createNewOffer(offer);
				offerTransactionService.createNewOffer(offerTransaction);
			}catch(Exception ex) {
				//TODO journalisation
				System.out.println(ex.toString());
			}
			return JsonUtils.toJson(offerTransaction);
		} else {
			return "INVALID OPERATION\n";
		}
	}

	// TODO rename
	// this is the method where Alicia accept an offer
	public String consultAwaitingOffers(String workData) {

		JsonObject json = JsonUtils.getFromJson(workData);
		String ownerID = json.get("ownerID").getAsString();

		List<OffersTransaction> offers = null;

		try {
			offers = offerTransactionService.getOffersBy(ownerID).stream()
					.filter(item -> item.getStatus() == OfferStatus.AWAITING_CONFIRMATION).collect(Collectors.toList());
		} catch(Exception ex) {
			//TODO journalisation
			System.out.println(ex.toString());
		}

		if (!offers.isEmpty()) {
			BBMLogger.infoln("Retrieved offers according to owner identifier.");
			return JsonUtils.toJson(offers);
		}

		return "No offers waiting for confirmation.\n";
	}

	// Alicia accepts an offer
	public String confirmAwaitingOffer(String workData) {

		JsonObject json = JsonUtils.getFromJson(workData);
		String transactionID = json.get("transactionID").getAsString();

		List<OffersTransaction> offers = null;

		try {
			offers = offerTransactionService.getOffersByTransactionID(transactionID);
		} catch(Exception ex) {
			//TODO journalisation
			System.out.println(ex.toString());
		}

		if(!offers.isEmpty()) {
			BBMLogger.infoln("Retrieved offers according to transaction identifier.");
			BBMLogger.infoln("Updated offer status to \"" + OfferStatus.CONFIRMED + "\".");

			//Update offer's fields  
			OffersTransaction offer = offers.get(0);
			offer.setStatus(OfferStatus.CONFIRMED);
			offer.setConfirmationDate(new Date().toString());

			// TODO mailing plus fin ? (genre autre method)
			usersHandler.sendMail(offer.getBuyerID(), offer.getFinalPrice(), offer.getOwnerID());
			offerTransactionService.createNewOffer(offer);
			return JsonUtils.toJson(offers);
		}

		return "INVALID OPERATION\n";
	}

	public String claimReceipt(String workData) {

		JsonObject json = JsonUtils.getFromJson(workData);
		String transactionID = json.get("transactionID").getAsString();

		List<OffersTransaction> offers = null;

		try {
			offers = offerTransactionService.getOffersByTransactionID(transactionID);
		} catch(Exception ex) {
			//TODO journalisation
			System.out.println(ex.toString());
		}

		if (!offers.isEmpty()) {
			//LOG 
			BBMLogger.infoln("Retrieved offers according to transaction identifier.");
			BBMLogger.infoln("Updated offer status to \"" + OfferStatus.AWAITING_RECEIPT_CONFIRMATION + "\".");

			//Update offer's fields 
			OffersTransaction offer = offers.get(0);
			offer.setStatus(OfferStatus.AWAITING_RECEIPT_CONFIRMATION);
			offer.setClientDepositDate(new Date().toString());

			//Update offer
			offerTransactionService.createNewOffer(offer);

			return JsonUtils.toJson(offer);
		}
		return "INVALID OPERATION\n";
	}

	public String confirmReceipt(String workData){
		JsonObject json = JsonUtils.getFromJson(workData);
		String transactionID = json.get("transactionID").getAsString();

		List<OffersTransaction> offers = null;

		try {
			offers = offerTransactionService.getOffersByTransactionID(transactionID);
		} catch(Exception ex) {
			System.out.println(ex.toString());
		}

		if (!offers.isEmpty()) {

			//LOG 
			BBMLogger.infoln("Retrieved offers according to transaction identifier.");
			BBMLogger.infoln("Updated offer status to \"" + OfferStatus.RECEIPT_DONE + "\".");

			//Update offer's fields  
			OffersTransaction offer = offers.get(0);
			offer.setStatus(OfferStatus.RECEIPT_DONE);
			offer.setClientDepositConfimationDate(new Date().toString());

			//Update offer in DB
			offerTransactionService.createNewOffer(offer);

			return JsonUtils.toJson(offers);
		}
		return "INVALID OPERATION\n";
	}

	public String claimDeposit(String workData) {
		JsonObject json = JsonUtils.getFromJson(workData);
		String transactionID = json.get("transactionID").getAsString();

		List<OffersTransaction> offers = null;

		try {
			offers = offerTransactionService.getOffersByTransactionID(transactionID);
		} catch(Exception ex) {
			//TODO journalisation
			System.out.println(ex.toString());
		}

		if (!offers.isEmpty()){

			BBMLogger.infoln("Retrieved offers according to transaction identifier.");
			OffersTransaction offer = offers.get(0);

			BBMLogger.infoln("Updated offer status to \"" + OfferStatus.AWAITING_DEPOSIT_CONFIRMATION + "\".");
			offer.setStatus(OfferStatus.AWAITING_DEPOSIT_CONFIRMATION);
			offer.setClientDepositConfimationDate(new Date().toString());

			try {
				offerTransactionService.createNewOffer(offer);
			} catch(Exception ex) {
				//TODO journalisation
				System.out.println(ex.toString());
			}
			return JsonUtils.toJson(offers);
		}

		return "INVALID OPERATION\n";
	}

	public String confirmDeposit(String workData) {

		JsonObject json = JsonUtils.getFromJson(workData);
		String transactionID = json.get("transactionID").getAsString();
		List<OffersTransaction> offers = null;

		//
		try {
			offers = offerTransactionService.getOffersByTransactionID(transactionID);
		} catch(Exception ex) {
			//TODO journalisation;
			System.out.println(ex.toString());
		}

		if (!offers.isEmpty()) {
			//TODO better get 0
			//We get the first element
			BBMLogger.infoln("Retrieved offers according to transaction identifier.");
			OffersTransaction offer = offers.get(0);

			//Set the offer's new parameters 
			offer.setStatus(OfferStatus.CLOSED);
			offer.setClientDepositConfimationDate(new Date().toString());

			//TODO rename createNewOffer
			offerTransactionService.createNewOffer(offer);

			//LOG   
			BBMLogger.infoln("Performing transaction, withdrawal account: " + offer.getBuyerID()
			+ ", deposit account: " + offer.getOwnerID() + ".");
			
			usersHandler.makeTransaction(offer.getOwnerID(), offer.getBuyerID(), offer.getFinalPrice());
			BBMLogger.infoln("Done.");

			List<PostedOffer> offersI = null;
			try {
				offersI = offerService.getOfferByID(offer.getOfferID());
			} catch(Exception ex) {
				//TODO journalisation
				System.out.println(ex.toString());
			}

			//Set the offer status to closed
			PostedOffer postoff = offersI.get(0);
			postoff.setStatus(OfferStatus.CLOSED);
			offerService.createNewOffer(postoff);

			//LOG
			BBMLogger.infoln("Updated offer status to \"" + OfferStatus.CLOSED + "\".");

			return JsonUtils.toJson(offers);
		}
		return "INVALID OPERATION\n";
	}

	private void logJson(PostedOffer offer){
		BBMLogger.infoln("{\"offerID\": \"" + offer.getOfferID() +"\",");
		BBMLogger.infoln("\"ownerID\": \"" + offer.getOwnerID() +"\",");
		BBMLogger.infoln("\"price\": \"" + offer.getPrice() +"\",");
		BBMLogger.infoln("\"startCity\": \"" + offer.getStartCity() +"\",");
		BBMLogger.infoln("\"endCity\": \"" + offer.getEndCity() +"\",");
		BBMLogger.infoln("\"capacity\": \"" + offer.getCapacity() +"\",");
		BBMLogger.infoln("\"status\": \"" + offer.getStatus() +"\",");
		BBMLogger.infoln("\"distance\": \"" + offer.getDistance() +"\"}");
	}
}
