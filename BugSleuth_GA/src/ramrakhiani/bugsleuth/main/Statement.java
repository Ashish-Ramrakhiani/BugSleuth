package ramrakhiani.bugsleuth.main;

public class Statement {
    private String value;
    private double susScore; // Additional attribute

    public Statement(String value, double susScore) {
        this.value = value;
        this.susScore = susScore;
    }

    public String getValue() {
        return value;
    }

    public double getsusScore() {
        return susScore;
    }
}
