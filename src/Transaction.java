import java.security.*;
import java.util.ArrayList;


public class Transaction {

	public String transactionId; //hash of transaction
	public PublicKey sender; 
	public PublicKey receiver;
	public float value;
	public byte[] signature;
	
	public ArrayList<TransactionInput> inputs=new ArrayList<>();
	public ArrayList<TransactionOutput> outputs=new ArrayList<>();
	
	
	private static int sequence=0;
	
	public Transaction(PublicKey from, PublicKey to, float value, ArrayList<TransactionInput> inputs){
		this.sender=from;
		this.receiver=to;
		this.value=value;
		this.inputs=inputs;
		
	}
	
	private String calculateHash(){
		sequence++;
		return StringUtil.applySHA256(StringUtil.getStringFromKey(sender)+StringUtil.getStringFromKey(receiver)+Float.toString(value)+sequence);
	}
	
	public void generateSignature(PrivateKey privateKey)throws Exception{
		String data=StringUtil.getStringFromKey(sender)+StringUtil.getStringFromKey(receiver)+Float.toString(value);
		signature=StringUtil.applyECDSASig(privateKey, data);
		
	}
	
	public boolean verifySignature() throws Exception{
		String data=StringUtil.getStringFromKey(sender)+StringUtil.getStringFromKey(receiver)+Float.toString(value);
		return StringUtil.verifyECDSASig(sender,data,signature);
	}
	
	
	public boolean processTransaction()throws Exception{
		if(verifySignature()==false){
			System.out.println("#Transaction signature verification failed");
			return  false;
		}
		
		for(TransactionInput input:inputs){
			input.UTXO=DemoChain.UTXOs.get(input.transactionOutputId);
		}
		
		if(getInputsValue()<DemoChain.minimumTransaction){
			System.out.println("#Transaction Inputs to small: "+getInputsValue());
			return false;
		}
		 
		float leftOver=getInputsValue() - value;
		transactionId=calculateHash();
		outputs.add(new TransactionOutput(this.receiver,value,transactionId));
		outputs.add(new TransactionOutput(this.sender,leftOver,transactionId));
		
		for(TransactionOutput output:outputs){
			DemoChain.UTXOs.put(output.id,output);
		}
		
		for(TransactionInput input:inputs){
			if(input.UTXO==null)
				continue;
			DemoChain.UTXOs.remove(input.UTXO.id);
		}
		
		return true;
	}
	
	public float getInputsValue(){
		float total=0;
		for(TransactionInput input:inputs){
			if(input.UTXO == null){
				continue;
			}
			total+= input.UTXO.value;
		}
		return total;
	}
	
	public float getOutputsValue(){
		float total=0;
		for(TransactionOutput output:outputs)
			total+=output.value;
		return total;
	}
	
}
