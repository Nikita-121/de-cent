import java.util.ArrayList;
import java.util.Date;


public class Block {
	
	public String hash;
	public String previousHash;
	public String merkelRoot;
	public ArrayList<Transaction> transactions=new ArrayList<>();
	private long timeStamp;
	private int nonce;
	
	public Block(String previousHash){
		this.previousHash=previousHash;
		this.timeStamp=new Date().getTime();
		this.hash=calculateHash();
	}
	
	public String calculateHash(){
		String calculatedHash=StringUtil.applySHA256(previousHash+Long.toString(timeStamp)+Integer.toString(nonce) + merkelRoot);
		return calculatedHash;
	}
	
	public void mineBlock(int difficulty){
		
		merkelRoot=StringUtil.getMerkelRoot(transactions);
		String target=new String(new char[difficulty]).replace('\0', '0');
		while(!hash.substring(0, difficulty).equals(target)){
			nonce++;
			hash=calculateHash();
			}
		System.out.println("Block mined: "+hash);
		
	}
	
	public boolean addTransaction(Transaction trans)throws Exception{
		if(trans==null)
			return false;
		if(previousHash!="0"){
			if(trans.processTransaction()!=true){
				System.out.println("#Transaction failed to process. Suspended.");
				return false;
			}
		}
		transactions.add(trans);
		System.out.println("Transaction successfully added to Block");
		return true;
	}
	
}
