package org.processmining.emdapplications.emdconceptdrift.solutiondata;

import org.apache.logging.log4j.message.Message;
import org.json.JSONObject;
import org.processmining.emdapplications.emdconceptdrift.language.OrderedStochasticLanguage;

public class EMDSolContainer implements Message {
	
	public static class Builder {
		private double emd;
		
		private NonZeroFlows nonZeroFlows;

		private double[][] cMat;
		
		private OrderedStochasticLanguage Ll;
		
		private OrderedStochasticLanguage Lr;
		
		public Builder() {
			emd = -1;
			nonZeroFlows = null;
			cMat = null;
			Ll = null;
			Lr = null;
		}
		
		public Builder addEMD(double emd) {
			this.emd = emd;
			return this;
		}

		public Builder addNonZeroFlows(NonZeroFlows nonZeroFlows) {
			this.nonZeroFlows = nonZeroFlows;
			return this;
		}

		public Builder addDistances(double[][] c) {
			this.cMat = c;
			return this;
		}

		public Builder addLangLeft(OrderedStochasticLanguage Ll) {
			this.Ll = Ll;
			return this;
		}
		
		public Builder addLangRight(OrderedStochasticLanguage Lr) {
			this.Lr = Lr;
			return this;
		}
		
		public EMDSolContainer build() {
			if(emd > -0.5 && nonZeroFlows != null && cMat != null && Ll != null && Lr != null) {
				return new EMDSolContainer(emd, cMat, nonZeroFlows, Ll, Lr); 
			}
			else
				return null;
		}
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6254243884344277387L;

	private final double emd;
	
	private final NonZeroFlows nonZeroFlows;

	private final double[][] cMat;
	
	private final OrderedStochasticLanguage Ll;
	
	private final OrderedStochasticLanguage Lr;
	
	public EMDSolContainer(double emd, double[][] cMat, NonZeroFlows nonZeroFlows, 
			OrderedStochasticLanguage Ll, OrderedStochasticLanguage Lr) {
		this.emd = emd;
		this.nonZeroFlows = nonZeroFlows;
		this.Ll = Ll;
		this.Lr = Lr;
		this.cMat = cMat;
	}

	@Override
	public String getFormattedMessage() {
		JSONObject jo = getJSON();
		return jo.toString();
	}
	
	public JSONObject getJSON() {
		JSONObject jo = new JSONObject();
		jo.put("EMD", this.emd);
		jo.put("Language left", Ll.toJson());
		jo.put("Language right", Lr.toJson());
		jo.put("Nonzero flows", nonZeroFlows.toJSON());
		jo.put("Distance", cMat);
		return jo;
		
	}

	@Override
	public String getFormat() {
		return "";
	}

	@Override
	public Object[] getParameters() {
		return new Object[]{emd, nonZeroFlows, Ll, Lr};
	}

	@Override
	public Throwable getThrowable() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public NonZeroFlows getNonZeroFlows() {
		return nonZeroFlows;
	}
	
	public OrderedStochasticLanguage getLanguageLeft() {
		return Ll;
	}

	public OrderedStochasticLanguage getLanguageRight() {
		return Lr;
	}
	
	public double getCost(int iSrc, int iTar) {
		return cMat[iSrc][iTar];
	}
	
	public double getEMD() {
		return emd;
	}
		
}
