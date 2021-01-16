package cz.gattserver.grass.control.speech;

public class ScoreTO {

	private Float score;
	private Float aScore;
	private Float lScore;

	public ScoreTO(Float score, Float aScore, Float lScore) {
		this.score = score;
		this.aScore = aScore;
		this.lScore = lScore;
	}

	public Float getScore() {
		return score;
	}

	public void setScore(Float score) {
		this.score = score;
	}

	public Float getaScore() {
		return aScore;
	}

	public void setaScore(Float aScore) {
		this.aScore = aScore;
	}

	public Float getlScore() {
		return lScore;
	}

	public void setlScore(Float lScore) {
		this.lScore = lScore;
	}
	
	public Float getSelectedScore() {
		return score;
	}

	@Override
	public String toString() {
		return "ScoreTO [score=" + score + ", aScore=" + aScore + ", lScore=" + lScore + "]";
	}

}
