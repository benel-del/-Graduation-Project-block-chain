package blockChain;

public class block {
	private String sign;
	private String content;
	
	block(String sign, String content){
		this.sign = sign;
		this.content = content;
	}

	public String getSign() {
		return sign;
	}
	public String getContent() {
		return content;
	}	
}