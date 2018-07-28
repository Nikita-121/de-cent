import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.GsonBuilder;

public class DemoChain {

	public static ArrayList<Block> blockchain=new ArrayList<>();
	public static HashMap<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>(); //list of all unspent transactions. 
	public static int difficulty=3;
	public static float minimumTransaction=0.1f;
	public static Wallet walletA;
	public static Wallet walletB;
	public static Transaction genesisTransaction;
	
	public static void main(String[] args)throws Exception{
		
		
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		
		walletA=new Wallet();
		walletB=new Wallet();
		Wallet coinbase=new Wallet();
		
		
		genesisTransaction=new Transaction(coinbase.publicKey, walletA.publicKey,100f ,null);
		genesisTransaction.generateSignature(coinbase.privateKey);
		genesisTransaction.transactionId="0";
		genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.receiver, genesisTransaction.value,genesisTransaction.transactionId));
		UTXOs.put(genesisTransaction.outputs.get(0).id,genesisTransaction.outputs.get(0));
		
		
		System.out.println("Creating and mining Genesis block..");
		System.out.println();
		Block genesis=new Block("0");
		genesis.addTransaction(genesisTransaction);
		addBlock(genesis);
		
		
		Block b1=new Block(genesis.hash);
		System.out.println("Wallet A's Balance: "+walletA.getBalance());
		System.out.println();
		System.out.println("Wallet A is attempting to send funds(30) to Wallet B ");
		System.out.println();
		b1.addTransaction(walletA.sendFunds(walletB.publicKey,30f));
		addBlock(b1);
		System.out.println("Wallet A's Balance: "+walletA.getBalance());
		System.out.println("Wallet B's Balance: "+walletB.getBalance());
		System.out.println();
		
		Block b2=new Block(b1.hash);
		System.out.println("Wallet A is attempting to send funds(500) to Wallet B");
		System.out.println();
		b2.addTransaction(walletA.sendFunds(walletB.publicKey, 500f));
		addBlock(b2);
		System.out.println("Wallet A's Balance: "+walletA.getBalance());
		System.out.println("Wallet B's Balance: "+walletB.getBalance());
		System.out.println();
		
		Block b3=new Block(b2.hash);
		System.out.println("Wallet B is attempting to send funds(20) to Wallet A");
		System.out.println();
		b3.addTransaction(walletB.sendFunds(walletA.publicKey, 20f));
		addBlock(b3);
		System.out.println("Wallet A's Balance: "+walletA.getBalance());
		System.out.println("Wallet B's Balance: "+walletB.getBalance());
		System.out.println();
		isChainValid();
		
		//tampering the transaction in block 1
		System.out.println("Attempting to tamper transaction in block 1");
		b1.transactions.get(0).receiver=new Wallet().publicKey; 
		isChainValid();
		
//		String blockchainJson=new GsonBuilder().setPrettyPrinting().create().toJson(blockchain);
//		System.out.println(blockchainJson);
		
	}
	
	public static void addBlock(Block block) {
		block.mineBlock(difficulty);
		blockchain.add(block);
		
		
	}

	public static boolean isChainValid()throws Exception{
		
		String hashTarget=new String(new char[difficulty]).replace('\0', '0');
		HashMap<String,TransactionOutput> tempUTXOs = new HashMap<String,TransactionOutput>(); //a temporary working list of unspent transactions at a given block state.
		tempUTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));

		if(blockchain.get(0).equals(blockchain.get(0).calculateHash()))
			return false;
		
		for(int j=1;j<blockchain.size();j++){
			Block curr=blockchain.get(j);
			Block prev=blockchain.get(j-1);
			
			if(!curr.hash.equals(curr.calculateHash()))
				{
				System.out.println("Current hash mismatch");
				return false;
				}
			if(!prev.hash.equals(curr.previousHash))
			{
				System.out.println("Previous hash mismatch");
				return false;
			}
			if(!curr.hash.substring(0,difficulty).equals(hashTarget))
			{
				System.out.println("Could not be mined");
				return false;
			}
			
			TransactionOutput temp;
			for(int i=0;i<curr.transactions.size();i++){
				Transaction current=curr.transactions.get(i);
				
				if(!current.verifySignature()){
					System.out.println("#Signature on Transaction(" + i+") is Invalid");
					return false;
				}
				
				if(current.getInputsValue()!=current.getOutputsValue()){
					System.out.println("Inputs and Outputs not equal");
					return false;
				}
				
				for(TransactionInput input:current.inputs){
					temp=tempUTXOs.get(input.transactionOutputId);
					
					if(temp==null){
						System.out.println("Referenced input on Transaction("+i+") value is missing.");
						return false;
					}
					if(input.UTXO.value!=temp.value){
						System.out.println("Referenced input Transaction("+i+") value is invalid.");
						return false;
					}
					tempUTXOs.remove(input.transactionOutputId);
					
					for(TransactionOutput output:current.outputs){
						tempUTXOs.put(output.id, output);
					}
					if(current.outputs.get(0).receiver!=current.receiver){
						System.out.println("#Transaction("+i+") output recepient  mismatch.");
						return false;
					}
					if(current.outputs.get(1).receiver!=current.sender){
						System.out.println("#Transaction("+i+") output sender  mismatch.");
						return false;
					}
					
				}
				
			}
			
			
		}
		System.out.println("Blockchain is valid");
		return true;
		
	}
	
}
