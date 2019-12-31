package com.main; 


public class GenerateAckCommand 
{

	public String createAckCommand(String message, int Command_Numerator) 
	{
		String ackoutdata = null;
		String data = null;
		String command_Numerator = null;
		String SystemCode = null;
		String messageType = null;
		String UNIT_ID = null;
		String Authentication_Code_Field = null;
		String Action_code = null;
		String Main_Acknowledge_number = null;
		String notUsingBytes = null;
		String checksum = null;
		command_Numerator = Integer.toHexString(Command_Numerator);
		if(command_Numerator.length() == 1)
			command_Numerator = "0"+command_Numerator;
		SystemCode = message.substring(0, 8);
		messageType = "04";
		UNIT_ID = message.substring(10, 18);
		Authentication_Code_Field = "00000000";
		Action_code = "00";
		Main_Acknowledge_number = message.substring(22, 24);
		notUsingBytes = "0000000000000000000000";
		data = messageType + UNIT_ID + command_Numerator
				+ Authentication_Code_Field + Action_code
				+ Main_Acknowledge_number + notUsingBytes;
		
		checksum = cal_checksum(data);
		
		ackoutdata = SystemCode + messageType + UNIT_ID + command_Numerator
				+ Authentication_Code_Field + Action_code
				+ Main_Acknowledge_number + notUsingBytes + checksum;
		return ackoutdata;
	}

	public String cal_checksum(String data) 
	{
		String checksum = null;
		int i_command_Numerator = 0;
		for (int i = 0; i < data.length(); i = i + 2) 
		{
			i_command_Numerator = i_command_Numerator
					+ Integer.parseInt(data.substring(i, i + 2), 16);
		}
		checksum = Integer.toHexString(i_command_Numerator);
		checksum = checksum.substring(checksum.length() - 2);
		return checksum;
	}
	
	public boolean validate_checksum(String data) 
	{
		String checksum = null;
		int i_command_Numerator = 0;
		boolean flag = false;
		String checksum_data = null;
		checksum_data = data.substring(8, data.length()-2);
		for (int i = 0; i < checksum_data.length(); i = i + 2) 
		{
			i_command_Numerator = i_command_Numerator
					+ Integer.parseInt(checksum_data.substring(i, i + 2), 16);
		}
		checksum = Integer.toHexString(i_command_Numerator);
		checksum = checksum.substring(checksum.length() - 2);
		if(checksum.equalsIgnoreCase(data.substring(data.length()-2)) )
			flag = true;
		return flag;
	}

}