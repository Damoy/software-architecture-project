package com.xaamruda.bbm.billing.calculator;

import com.xaamruda.bbm.commons.logging.BBMLogger;

/**
 * Class for billing calculation
 */
public class Calculator implements ICalculator {

	private double companyPoints;
	private double userPoints;

	public Calculator() {
		this.companyPoints = 0.0;
		this.userPoints = 0.0;
	}

	@Override
	public void calcul_price_base(double weight, double distance, double volume, double day) {
		double calcul = fullAddition(weight, distance, volume, day);
		this.userPoints = (calcul);
	}

	@Override
	public void advance_date_with_offer(double date, int offer){
		double res = offer + conversionDay(date);
		this.userPoints = (res);
	}

	@Override
	public void finalConfirmation(double point){
		BBMLogger.infoln("Computing points repartition...");
		this.userPoints = (point/100.0) * 90.0;
		this.companyPoints = (point/100.0) * 10.0;
	}

	@Override
	public void reset(){
		BBMLogger.infoln("Points reset.");
		this.userPoints = 0.0;
		this.companyPoints = 0.0;
	}

	//////////// SETTER ////////////
	public void setCompanyPoints(int total) {
		BBMLogger.infoln("Setting company points...");
		this.companyPoints = total;
	}

	public void setUserPoints(int total) {
		BBMLogger.infoln("Setting user points...");
		this.companyPoints = total;
	}

	//////////// GETTER ////////////
	public double getCompanyPoints() {
		BBMLogger.infoln("Retrieving company points...");
		return this.companyPoints;
	}

	public double getUserPoints() {
		BBMLogger.infoln("Retrieving user points...");
		return this.userPoints;
	}

	private double fullAddition(double weight, double distance, double volume, double day) {
		double weightPoint = conversionWeight(weight);
		double distancePoint = conversionDistance(distance);
		double volumePoint = conversionVolume(volume);
		double reservationPoint = conversionDay(day);
		return (weightPoint + distancePoint + volumePoint + reservationPoint);
	}

	//////////// CONVERTER ////////////
	private double conversionWeight(double weight) {
		return Math.floor(weight/1.0);
	}

	private double conversionDistance(double distance) {
		return Math.floor(distance/1.0);
	}

	private double conversionVolume(double volume) {
		return Math.floor(volume/1.0);
	}

	private double conversionDay(double day) {
		double res = 0;
		if(day > 3){
			res = 10.0;
		}else{
			res = 5.0;
		}
		return res;
	}
}
