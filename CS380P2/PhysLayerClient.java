package CS380P2;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Hashtable;

public class PhysLayerClient {

	public static void main(String[] args) throws Exception{
		try (Socket socket = new Socket("18.221.102.182", 38002)) {
			System.out.println("Connected to server.");

			InputStream is = socket.getInputStream();
			int a = 0;
			double avg = 0;
			for(int i = 0; i < 64; i++){
				a+=is.read();
			}
			avg = (double)a/64;

			System.out.printf("Baseline established from preamble: " + "%.2f", avg);
			System.out.println();
			int[] stuff = new int[320];
			for(int i = 0; i < 320; i++){
				if(is.read()>avg){
					stuff[i]=1;
				} else {
					stuff[i]=0;
				}
			}

			//NRZI decoding
			int prev = 0;
			for(int i = 0; i < 320; i++){
				int temp = stuff[i];
				stuff[i] = (prev^stuff[i]);
				prev = temp;
			}

			//4B/5B thing
			Hashtable<String, Integer> ht = new Hashtable<String, Integer>();

			ht.put("11110", 0b0000);
			ht.put("01001", 0b0001);
			ht.put("10100", 0b0010);
			ht.put("10101", 0b0011);
			ht.put("01010", 0b0100);
			ht.put("01011", 0b0101);
			ht.put("01110", 0b0110);
			ht.put("01111", 0b0111);
			
			ht.put("10010", 0b1000);
			ht.put("10011", 0b1001);
			ht.put("10110", 0b1010);
			ht.put("10111", 0b1011);
			ht.put("11010", 0b1100);
			ht.put("11011", 0b1101);
			ht.put("11100", 0b1110);
			ht.put("11101", 0b1111);	
			
			int potatoes = 0;

			System.out.print("Received 32 bytes: ");
			byte[] jiggs = new byte[32];
			for(int j = 0; j < 32; j++){
				String b = "";
				String c = "";
				for(int i = 10*j; i < 10*j+5; i++){
					b+=stuff[i];
				}
				potatoes = ht.get(b)*16;
				for(int i = 10*j+5; i < 10*j+10; i++){
					c+=stuff[i];
				}
				potatoes += ht.get(c);
				System.out.print(String.format("%02x", potatoes).toUpperCase());
				jiggs[j] = (byte) potatoes;
			}
			System.out.println();
			OutputStream os = socket.getOutputStream();
			os.write(jiggs);
			
			if(is.read() == 1){
				System.out.println("Response good.");
			} else {
				System.out.println("Response bad.");
			}
			System.out.println("Disconnected from server");
		}
	}

}