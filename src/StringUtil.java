import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.security.*;

public class StringUtil {

	// applies SHA-256 to the input string
	public static String applySHA256(String input) {
		try {
			MessageDigest msgdg = MessageDigest.getInstance("SHA-256");
			byte[] hash = msgdg.digest(input.getBytes("UTF-8"));

			// contains hash as hexadecimal
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < hash.length; i++) {
				String hex = Integer.toHexString(0xff & hash[i]);
				if (hex.length() == 1)
					hexString.append('0');
				hexString.append(hex);
			}
			return hexString.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}
	
	public static byte[] applyECDSASig(PrivateKey privateKey, String input)throws Exception{
		Signature dsa;
		byte[] output=new byte[0];
		try{
			dsa=Signature.getInstance("ECDSA","BC");
			dsa.initSign(privateKey);
			byte[] strByte=input.getBytes();
			dsa.update(strByte);
			byte[] realSig=dsa.sign();
			output=realSig;
		}
		catch(Exception e){
			throw new Exception(e);
		}
		return output;
	}
	
	public static boolean verifyECDSASig(PublicKey publicKey, String data, byte[] signature)throws Exception{
		try{
			Signature ecdsaVerify=Signature.getInstance("ECDSA","BC");
			ecdsaVerify.initVerify(publicKey);
			ecdsaVerify.update(data.getBytes());
			return ecdsaVerify.verify(signature);
		}
		catch(Exception e){
			throw new Exception(e);
		}
	}
	public static String getStringFromKey(Key key){
		return  Base64.getEncoder().encodeToString(key.getEncoded());
	}
	
	public static String getMerkelRoot(ArrayList<Transaction> transactions){
		int count=transactions.size();
		ArrayList<String> previousLayer=new ArrayList<>();
		for(Transaction trans:transactions){
			previousLayer.add(trans.transactionId);
		}
		ArrayList<String> treelayer=previousLayer;
		while(count>1){
			treelayer=new ArrayList<>();
			for(int i=1;i<previousLayer.size();i++){
				 treelayer.add(applySHA256(previousLayer.get(i-1)+previousLayer.get(i)));
				 
			}
			count=treelayer.size();
			previousLayer=treelayer;
		}
		String merkelRoot=(treelayer.size()==1)?treelayer.get(0):"";
		return merkelRoot;
	}

}


