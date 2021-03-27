package blockChain;

public class block {
	private String sign;
	private String content;
	private String state;
	
	public block(String sign, String content){
		this.sign = sign;
		this.content = content;
		this.state = "";
	}
	
	public block(String sign, String content, String state){
		this.sign = sign;
		this.content = content;
		this.state = state;
	}

	public String getSign() {
		return sign;
	}
	public String getContent() {
		return content;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
}