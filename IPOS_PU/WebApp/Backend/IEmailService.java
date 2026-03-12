public interface IEmailService {

	/**
	 * Allows other subsystems to use PU to send emails. Email: email address you're sending to; content: content of email; reference: category of email (if it is an invoice, an advert etc); sender: email address that is sending the email; subsystem: which IPOS subsystem generated the email.
	 * @param email
	 * @param content
	 * @param reference
	 * @param sender
	 * @param sybsystem
	 */
	boolean produceEmail(string email, string content, string reference, string sender, string sybsystem);

}