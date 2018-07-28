import java.security.*;

public class TransactionOutput {

	public String id;
	public PublicKey receiver;
	public float value;
	public String parentTransactionID;
	
	public TransactionOutput(PublicKey receiver, float value, String parentTransactionID){
		this.receiver=receiver;
		this.value=value;
		this.parentTransactionID=parentTransactionID;
		this.id=StringUtil.applySHA256(StringUtil.getStringFromKey(receiver)+Float.toString(value)+parentTransactionID);
	}
	
	public boolean isCoinMine(PublicKey publicKey){
		return publicKey==receiver;
	}
}
