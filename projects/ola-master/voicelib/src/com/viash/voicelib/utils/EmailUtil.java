package com.viash.voicelib.utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class EmailUtil {
	
	private static final String TAG = "EmailUtil";
	private static final boolean DEBUG = false;
	
    // message column mapping
	public static final int COLUMN_ID = 0;
    public static final int COLUMN_MAILBOX_KEY = 1;
    public static final int COLUMN_ACCOUNT_KEY = 2;
    public static final int COLUMN_DISPLAY_NAME = 3;
    public static final int COLUMN_SUBJECT = 4;
    public static final int COLUMN_DATE = 5;
    public static final int COLUMN_READ = 6;
    public static final int COLUMN_FAVORITE = 7;
    public static final int COLUMN_ATTACHMENTS = 8;
    public static final int COLUMN_FLAGS = 9;
    public static final int COLUMN_SNIPPET = 10;
    
    public static class EmailData {
    	// all classes share this id for email objects
        public static final String RECORD_ID = "_id";
        // references to other Email objects in the database
        // foreign key to the Mailbox holding this message [INDEX]
        public static final String MAILBOX_KEY = "mailboxKey";
        // foreign key to the Account holding this message
        public static final String ACCOUNT_KEY = "accountKey";
        // the display name of the account (user-settable)
        public static final String DISPLAY_NAME = "displayName";
        // message subject
        public static final String SUBJECT = "subject";
        // the time (millisecond) as shown to the user in a message list [INDEX]
        public static final String TIMESTAMP = "timeStamp";
        // boolean, unread = 0, read = 1 [INDEX]
        public static final String FLAG_READ = "flagRead";
        // boolean, unflagged = 0, flagged (favorite) = 1
        public static final String FLAG_FAVORITE = "flagFavorite";
        // boolean, no attachment = 0, attachment = 1
        public static final String FLAG_ATTACHMENT = "flagAttachment";
        // bit field for flags which we'll not be selecting on
        public static final String FLAGS = "flags";
        // a text "snippet" derived from the body of the message
        public static final String SNIPPET = "snippet";
        
        // to refer to a specific message, use ContentUris.withAppendedId(CONTENT_URI, id)
        public static final String AUTHORITY = "com.android.email.provider";
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/message");
        // public static final Uri CONTENT_URI = Uri.parse(EmailContent.CONTENT_URI + "/message");
        
        // the following flags indicate messages that are determined to be incoming meeting related (e.g. invites from others)
        public static final int FLAG_INCOMING_MEETING_INVITE = 1 << 2;
        public static final int FLAG_REPLIED_TO = 1 << 18;
        public static final int FLAG_FORWARDED = 1 << 19;
        
        // email value objects
        public long messageId;
        public long mailboxId;
        public long accountId;
        public boolean isRead;
        public boolean isFavorite;
        public boolean hasInvite;
        public boolean hasBeenRepliedTo;
        public boolean hasBeenForwarded;
        public boolean hasAttachment;
        public Date mailDate;
        public String mailSender;
        public String mailSubject;
        public String mailSnippet;
        
		public long getMessageId() {
			return messageId;
		}
		
		public void setMessageId(long messageId) {
			this.messageId = messageId;
		}
		
		public long getMailboxId() {
			return mailboxId;
		}
		
		public void setMailboxId(long mailboxId) {
			this.mailboxId = mailboxId;
		}
		
		public long getAccountId() {
			return accountId;
		}
		
		public void setAccountId(long accountId) {
			this.accountId = accountId;
		}
		
		public boolean isRead() {
			return isRead;
		}
		
		public void setRead(boolean isRead) {
			this.isRead = isRead;
		}
		
		public boolean isFavorite() {
			return isFavorite;
		}
		
		public void setFavorite(boolean isFavorite) {
			this.isFavorite = isFavorite;
		}
		
		public boolean isHasInvite() {
			return hasInvite;
		}
		
		public void setHasInvite(boolean hasInvite) {
			this.hasInvite = hasInvite;
		}
		
		public boolean isHasBeenRepliedTo() {
			return hasBeenRepliedTo;
		}
		
		public void setHasBeenRepliedTo(boolean hasBeenRepliedTo) {
			this.hasBeenRepliedTo = hasBeenRepliedTo;
		}
		
		public boolean isHasBeenForwarded() {
			return hasBeenForwarded;
		}
		
		public void setHasBeenForwarded(boolean hasBeenForwarded) {
			this.hasBeenForwarded = hasBeenForwarded;
		}
		
		public boolean isHasAttachment() {
			return hasAttachment;
		}
		
		public void setHasAttachment(boolean hasAttachment) {
			this.hasAttachment = hasAttachment;
		}
		
		public Date getMailDate() {
			return mailDate;
		}
		
		public void setMailDate(Date mailDate) {
			this.mailDate = mailDate;
		}

		public String getMailSender() {
			return mailSender;
		}

		public void setMailSender(String mailSender) {
			this.mailSender = mailSender;
		}

		public String getMailSubject() {
			return mailSubject;
		}

		public void setMailSubject(String mailSubject) {
			this.mailSubject = mailSubject;
		}

		
		public String getMailSnippet() {
			return mailSnippet;
		}
		

		public void setMailSnippet(String mailSnippet) {
			this.mailSnippet = mailSnippet;
		}
         
    }
	
    /**
     * collect the email data object which exists in mailbox 
     * @param context
     * @param isToday
     * @return
     */
	public static List<EmailData> getEmailData(Context context, boolean isToday) {
		
		// set message projection
		final String[] MESSAGE_PROJECTION = new String[] {
				EmailData.RECORD_ID, EmailData.MAILBOX_KEY, EmailData.ACCOUNT_KEY,
				EmailData.DISPLAY_NAME, EmailData.SUBJECT, EmailData.TIMESTAMP,
				EmailData.FLAG_READ, EmailData.FLAG_FAVORITE, EmailData.FLAG_ATTACHMENT,
				EmailData.FLAGS, EmailData.SNIPPET
		};
		
		// set the mailbox id
		final StringBuilder selection = new StringBuilder();
        selection.append(EmailData.MAILBOX_KEY).append('=').append(6); // for mailbox id 6
        
        // set the query string
        Cursor cursor = context.getContentResolver().query(EmailData.CONTENT_URI, MESSAGE_PROJECTION, 
				null, null, EmailData.TIMESTAMP + " DESC");
        
		int todayEmailNumber = 0;
		int totalEmailNumber = 0;
		ArrayList<EmailData> emailDataList = new ArrayList<EmailUtil.EmailData>();
		while (cursor.moveToNext()) {
			
			EmailData emailDataObj = new EmailData();
			long messageId = cursor.getLong(COLUMN_ID);
			long mailboxId = cursor.getLong(COLUMN_MAILBOX_KEY);
	        final long accountId = cursor.getLong(COLUMN_ACCOUNT_KEY);
	        
	        boolean isRead = cursor.getInt(COLUMN_READ) != 0;
	        boolean isFavorite = cursor.getInt(COLUMN_FAVORITE) != 0;
	        final int flags = cursor.getInt(COLUMN_FLAGS);
	        boolean hasInvite = (flags & EmailData.FLAG_INCOMING_MEETING_INVITE) != 0;
	        boolean hasBeenRepliedTo = (flags & EmailData.FLAG_REPLIED_TO) != 0;
	        boolean hasBeenForwarded = (flags & EmailData.FLAG_FORWARDED) != 0;
	        boolean hasAttachment = cursor.getInt(COLUMN_ATTACHMENTS) != 0;
	        long timestamp = cursor.getLong(COLUMN_DATE);
	        Date mailDate = new Date(timestamp);
	        Calendar cal = Calendar.getInstance();
	        cal.set(Calendar.HOUR_OF_DAY, 0);
	        cal.set(Calendar.MINUTE, 0);
	        cal.set(Calendar.SECOND, 0);
	        cal.set(Calendar.MILLISECOND, 0);
	        Date today = cal.getTime();
	        String emailSender = cursor.getString(COLUMN_DISPLAY_NAME);
	        String emailSubject = cursor.getString(COLUMN_SUBJECT);
	        String emailSnippet = cursor.getString(COLUMN_SNIPPET);
	        
	        emailDataObj.setAccountId(accountId);
	        emailDataObj.setFavorite(isFavorite);
	        emailDataObj.setHasAttachment(hasAttachment);
	        emailDataObj.setHasBeenForwarded(hasBeenForwarded);
	        emailDataObj.setHasBeenRepliedTo(hasBeenRepliedTo);
	        emailDataObj.setHasInvite(hasInvite);
	        emailDataObj.setMailboxId(mailboxId);
	        emailDataObj.setMailDate(mailDate);
	        emailDataObj.setMessageId(messageId);
	        emailDataObj.setRead(isRead);
	        emailDataObj.setMailSender(emailSender);
	        emailDataObj.setMailSubject(emailSubject);
	        emailDataObj.setMailSnippet(emailSnippet);
	        
	        if (isToday) {
	        	// collect todays email data objects in mailbox
		        if (mailDate.after(today)) {
		        	todayEmailNumber++;
		        	emailDataList.add(emailDataObj);
		        	if (DEBUG) Log.d(TAG, "You got " + todayEmailNumber + " emails today. Return todays email objects.");
		        }
	        } else {
	        	// collect total email data objects in mailbox
	        	totalEmailNumber ++;
	        	emailDataList.add(emailDataObj);
	        	if (DEBUG) Log.d(TAG, "You got " + totalEmailNumber + " emails. Return total email objects.");
	        }
	        if (DEBUG) Log.d(TAG, mailboxId + " : " + emailSender + " : " + emailSubject + " " + mailDate + " isRead:" + isRead);
	        
		}
		
		return emailDataList;
	}
	
	/**
	 * send email
	 * @param context
	 * @param to
	 * @param cc
	 * @param bcc
	 * @param subject
	 * @param content
	 */
	public static void sendEmail(Context context, String[] to, String[] cc, String[] bcc, String subject, String content) {
		Intent sendEmailIntent = new Intent(android.content.Intent.ACTION_SEND);
		sendEmailIntent.setType("text/html");
		sendEmailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, to);
		sendEmailIntent.putExtra(android.content.Intent.EXTRA_CC, cc);
		sendEmailIntent.putExtra(android.content.Intent.EXTRA_BCC, bcc);
		sendEmailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
		sendEmailIntent.putExtra(android.content.Intent.EXTRA_TEXT, content);
		sendEmailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(sendEmailIntent);
	}
	
	private static final String ACTIVITY_INTENT_SCHEME = "content";
    private static final String ACTIVITY_INTENT_HOST = "ui.email.android.com";
    private static final String ACCOUNT_ID_PARAM = "ACCOUNT_ID";
    private static final String MAILBOX_ID_PARAM = "MAILBOX_ID";
    private static final String MESSAGE_ID_PARAM = "MESSAGE_ID";
    
    /**
     * view specific email content by using OS Email package
     * @param context	activity context
     * @param accountId the account id
     * @param mailboxId the mail box id
     * @param messageId the message id
     */
    public static void viewEmail(Context context, long accountId, long mailboxId, long messageId) {
		Uri.Builder uriBuilder = new Uri.Builder();
		uriBuilder.scheme(ACTIVITY_INTENT_SCHEME);
		uriBuilder.authority(ACTIVITY_INTENT_HOST);
		uriBuilder.path("/view/mailbox");
		uriBuilder.appendQueryParameter(ACCOUNT_ID_PARAM, Long.toString(accountId));
		uriBuilder.appendQueryParameter(MAILBOX_ID_PARAM, Long.toString(mailboxId));
		uriBuilder.appendQueryParameter(MESSAGE_ID_PARAM, Long.toString(messageId));
        Intent viewMailIntent = new Intent(Intent.ACTION_MAIN, uriBuilder.build());
        viewMailIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        viewMailIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        viewMailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // viewMailIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        viewMailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(viewMailIntent);
	}
	
}
