package Helpers;

import java.math.BigInteger;

public class HelperBit {

	  public static int GetBits(int value)
      {
		  BigInteger aux = new BigInteger(String.valueOf(value));
          int result = GetBits(aux);

          return (result);
      }


      public static int GetBits(BigInteger value)
      {
          int result = 0;

          BigInteger numero = value;

          while(numero.compareTo(BigInteger.ZERO)>0)
          {
              result++;
              numero = numero.shiftRight(1);
          }

          return (result);
      }	

      public static int GetBitsFromDecimalDigits(int exponent)
      {
          BigInteger base10 = new BigInteger("10");
          BigInteger maxValue = base10.pow(exponent).subtract(BigInteger.ONE);

          int result = GetBits(maxValue);

          return (result);
      }


      /*public static String ToBits(String value)
      {
          StringBuilder sb = new StringBuilder();

          if (value!= null && value != "")
          {
              for (int i = 0; i < value.length(); i++)
                  sb.append(ToBits(value.charAt(i)));
          }

          return (sb.toString());
      }*/
      

      	public static String ToBits(String value)
      	{
      		StringBuilder sb=new  StringBuilder();
      		
      		if(value!= null && value != "")
      		{
      			 for (char c : value.toCharArray()) 
      			 {
      		        String bits =Integer.toBinaryString((int)c);
      		        
      		        while(bits.length()<8)
      		        	bits = '0'+bits;
      		        	
      		        sb.append(bits);
      			 }
      		}
      		
      		return(sb.toString());
      	}
      	
      	


      /*public static String ToBits(char value)
      {
    	  
          String result = String.format("%8s", Integer.toBinaryString(value)); //String.format("%08s", Integer.toBinaryString(value)); 
          result = result.replace(" ", "0");
          
          return (result);
      }*/


      public static String ToString(String bitArray)
      {
          String result = "";

          if (bitArray == null)
              return (result);

          
          /*if (bitArray.length() % 8 != 0)
              throw new ArgumentException(string.Format("The bitArray length ({0}) must be multiuple of 8", bitArray.Length));*/

          String str = bitArray;

          byte[] bytes = new byte[bitArray.length() / 8];
          int j = 0;

          while (str.length() > 0)
          {
        	  
              bytes[j++] = (byte)Integer.parseInt(str.substring(0, 8), 2);
            		  
              if (str.length() >= 8)
                  str = str.substring(8);
          }

          StringBuilder sb = new StringBuilder();
          for(int i=0;i<bytes.length; i++)
        	  sb.append((char)bytes[i]);
          
          result = sb.toString();

          return (result);
      }
	
}
