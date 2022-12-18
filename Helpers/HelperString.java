package Helpers;

public class HelperString {

	/// <summary>
    /// 
    /// </summary>
    public static String Fill(String value, int length, char filler)
    {
        String result = value;

        if (value != null)
        {
            if (value.length() < length)
            {
                String fill = "";
                for (int i = value.length(); i < length; i++)
                    fill += String.valueOf(filler);

                result =fill+value;
            }
        }

        return (result);
    }
}
