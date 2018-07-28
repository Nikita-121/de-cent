
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

import org.bouncycastle.crypto.tls.HashAlgorithm;

public class Wallet {

	public PrivateKey privateKey;
	public PublicKey publicKey;
	public HashMap<String, TransactionOutput> UTXOs=new HashMap<>();
	public Wallet(){
		generateKeyPair();
	}

	private void generateKeyPair() {

		try{
			KeyPairGenerator keyGen= KeyPairGenerator.getInstance("ECDSA","BC");
			SecureRandom random=SecureRandom.getInstance("SHA1PRNG");
			ECGenParameterSpec ecSpec=new ECGenParameterSpec("prime192v1");
			
			//Initialization
			keyGen.initialize(ecSpec,random);
			KeyPair keyPair=keyGen.generateKeyPair();
			
			//Setting public and private keys using keypair
			privateKey=keyPair.getPrivate();
			publicKey=keyPair.getPublic();
		
		}
		
		catch(Exception e){
			throw new RuntimeException(e);
		}
		
	}

	
	public float getBalance(){
		float total=0;
		for(Map.Entry<String,TransactionOutput> item:DemoChain.UTXOs.entrySet()){
			TransactionOutput UTXO=item.getValue();
			if(UTXO.isCoinMine(publicKey)){
				UTXOs.put(UTXO.id, UTXO);
				total+=UTXO.value;
			}
		}
		return total;
	}
	
	public Transaction sendFunds(PublicKey receiver, float value)throws Exception{
		if(getBalance()<value){
			System.out.println("#Not enough funds to send transaction. Transaction Request suspended.");
			return null;
		}
		
		
		ArrayList<TransactionInput> inputs=new ArrayList<>();
		
		float total=0;
		for(Map.Entry<String,TransactionOutput> item:UTXOs.entrySet()){
			TransactionOutput UTXO=item.getValue();
			total+=UTXO.value;
			inputs.add(new TransactionInput(UTXO.id));
			if(total>=value)
				break;
		}
		
		Transaction newTrans=new Transaction(publicKey, receiver, value, inputs);
		newTrans.generateSignature(privateKey);
		
		for(TransactionInput input:inputs){
			UTXOs.remove(input.transactionOutputId);
		}
		
		return newTrans;
	}
	
	
}
