package utils;


import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;

import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import upnotify_bot.UpnotifyBot;


/**
 * Handles the functions with which telegram messages are sent. Kind of like 'front end'
 * 
 */
public class MessageUtils {
	
	private static MessageUtils single_instance = null;
	
	public static MessageUtils getMessageUtils() {
		if (single_instance == null) {
			single_instance = new MessageUtils();
			System.out.println("Instance of 'MessageUtils' has been created");
		}
		return single_instance;
		
	}
	// Has only a private constructor, so that only one instance can exist
	private MessageUtils() {}
	
	
	/**
	 * Answers with our debug message, containing thread info together with all the data from
	 * the update. Removes the debug message in @WAIT_UNTIL_MESSAGE_DELETE seconds
	 * @param ub Reference to our bot object.
	 * @param threadId ID of the thread within the pool.
	 * @param chatId ID of the chat that the debug message is to be sent to.
	 * @param update the update that is to be converted to string and printed.
	 * @return true if message had been sent successfully, false otherwise.
	 */
	public boolean sendDebugMessage(UpnotifyBot ub, String threadId, String chatId, Update update) {
		String debugText = "ok\n" 
				+ "thread ID:" + threadId + "\n Message: \n"
				+ update;
		SendMessage debugMessage = new SendMessage(chatId, debugText); // Create a SendMessage object with mandatory fields
		Message mg;
	
	
		try {
			mg = ub.execute(debugMessage); 
        } catch (TelegramApiException e) {
            //TODO logging
        	e.printStackTrace();
            return false;
        }
		
		// wait for given time
		try {
			System.out.println("Thread " + threadId + " is Waiting for " + (float)Config.getConfig().WAIT_UNTIL_MESSAGE_DELETE / 1000 + " seconds.");
			
			Thread.sleep(Config.getConfig().WAIT_UNTIL_MESSAGE_DELETE);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        
		// remove debug message
		DeleteMessage dm = new DeleteMessage();
		dm.setChatId(chatId);
		dm.setMessageId(mg.getMessageId());
        try {
			ub.execute(dm); // executeAsync is similar to execute, but doesn't validate if the message has arrived to telegram.. e.g. we don't really care if it is actually removed here...
		} catch (TelegramApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        // remove the debugmsg command message
        dm.setMessageId(update.getMessage().getMessageId());
        try {
			ub.execute(dm);
		} catch (TelegramApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return true;
	}
	
	/**
	 * @TODO MOVE THE WEB RELATED CODE TO A DIFFERENT CLASS
	 * @PROBLEM https://tau.edu.tr doesn't work cuz it is not signed etc
	 * @return
	 */
	public boolean checkSiteHTTPResponse(UpnotifyBot ub, String threadId, String chatId, String url){
		String code = WebUtils.getWebUtils().getHTTPResponseFromUrl(url);
		SendMessage sm = new SendMessage(chatId, "Response code for " + url + " is as follows: " + code);
		try {
			ub.execute(sm);
		} catch (TelegramApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	
	}
	
	public boolean checkIfHTMLBodyStatic(UpnotifyBot ub, String chatId, String url){
		WebUtils wu = WebUtils.getWebUtils();
		String body = wu.getHTMLBodyStringFromUrl(url);
		try {
			Thread.sleep(Config.getConfig().WAIT_STATIC_CHECK);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String bodyNew = wu.getHTMLBodyStringFromUrl(url);
		
		
		
		SendMessage sm = new SendMessage(chatId, 
				String.valueOf(body.contentEquals(bodyNew)));
		try {
			ub.execute(sm);
		} catch (TelegramApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		return true;
		
		
		
	}
//	
//	public boolean insertRequest() {
//		
//	}
	public boolean sendWelcomeMessage(UpnotifyBot ub, String threadId, String chatId, Update update) {
		SendPhoto sp = new SendPhoto();
		sp.setChatId(chatId);
		sp.setPhoto(new InputFile( new File ("src/main/resources/IMAGES/welcome-red-sign-760.png")));
		try {
			ub.execute(sp);
		} catch (TelegramApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
	public void sendHelpMessage(UpnotifyBot ub, String chatId, Update update, objects.User upUser) {
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder();
		sb.append("<b> Your Info </b>");
		sb.append("\n<i>"
				+ "Your level is: <b>" + upUser.checkLevel + "</b> and with that, you can "
				+ "have your upnotify request run once every "
				+ Config.getConfig().MIN_WAIT_LEVEL[upUser.checkLevel] + " minutes or less often if you request so."
				+ "</i>");
		sb.append("\n<b> Bot Info </b>");
		sb.append("\n<code> System.out.println(\"Hello Telegram!\"); </code>");
		String helpText = sb.toString();
		System.out.println(helpText);
				
		//		String helpText = "Hi\\!"
//				+ "# \n\n__YOUR INFO__\n"
//					+ "Your level is: " + upUser.checkLevel + "and with that, you can"
//						+ " have your upnotify request run once every "
//						+ Config.getConfig().MIN_WAIT_LEVEL[upUser.checkLevel] + " minutes or less often if you request.\n"
//				+ "# \n\n_BOT INFO_\n"
//					+ "* This bot cares about your privacy. Bot has access to all *private* messages _that you send to it directly_ but in groups, this bot has no access to "
//					+ "messages that don't start with a '/' (messages that are not commands) + to learn more read following: {INSERT_TELEGRAPH_LINK_HERE} "
//					+ "\n This bot will notify you for changes in web pages or web page sections in determined intervals."
//				+ "# \n\n_COMMANDS_"
//					+ "\n/msginfo foo	->	Sends all the info that the bot receives with any message you send it, so that you can know how much of your information is seen by the bot."
//					+ "\n ...blabla"
//				+ "# \n\n_OTHER COMMUNICATION_"
//					+ "hi	->	Bot will send you a welcome photo";
		SendMessage sm = new SendMessage(chatId, helpText);
		sm.setParseMode(ParseMode.HTML);
		try {
			ub.execute(sm);
		} catch (TelegramApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
				
		
	}
	
	
	
	
}