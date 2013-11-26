package models.snipstory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import play.data.format.Formats;
import play.data.validation.Constraints.Email;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.MinLength;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

@Entity
public class User extends Model {

	private static final long serialVersionUID = 1L;
	
	public static Finder<Long, User> find = new Finder<Long, User>(Long.class, User.class);

	@Id
	public long id;

	@Email
	@Required
	@MaxLength(255)
	@Column(nullable = false, unique = true)
	public String email;
	
	@Required
	@MaxLength(255)
	@Column(nullable = false)
	public String name;
	
	@Required
	@Formats.DateTime(pattern="MM/dd/yyyy")
	@Temporal(TemporalType.DATE)
	@Column(nullable = false)
	public Date birthdate;
	
	@Column(length = 32, nullable = false)
	public String salt;
	
	/**
	 * SHA-256 Hash of SHA-256 Hash of password + salt
	 */
	@Column(length = 64)
	public String saltedPasswordHash;
	
	@Temporal(TemporalType.TIMESTAMP)
	public Date lastLogin;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(nullable = false)
	public Date creation = new Date();
	
	@Temporal(TemporalType.TIMESTAMP)
	public Date activation;
	
	@Column(nullable = false)
	public int numLogins = 0;
		
	@Required
	@MaxLength(64)
	@MinLength(64)
	@Transient
	public String passwordHash;

	public User(String email, String name, Date birthdate) {
		this.email = email;
		this.name = name;
		this.birthdate = birthdate;
		this.creation = new Date();
	}
	
	public String validate() {
		//validating new user form input
		//check for user based on email for duplicates
		User existingUser = User.find.where().eq("email", email).findUnique();
		if (existingUser != null) {
			return "Account already exists for that email";
		} else {
			return null;
		}
    }
		
	public static User authenticate(String email, String passwordHash) {
		User user = find.where().eq("email", email).findUnique();
		if (user == null)
			return null;
		//check password hash
		String saltedHash = saltPasswordHash(passwordHash, user.salt);
		if (saltedHash.equals(user.saltedPasswordHash)) {
			return user;
		} else 
			return null;
    }

	private static String saltPasswordHash(String passwordHash, String salt) {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
		byte [] hashBytes = digest.digest((passwordHash + salt).getBytes(StandardCharsets.UTF_8));
		return bytesToHexString(hashBytes);
	}
	
	private static String bytesToHexString(byte[] bytes) {
		StringBuffer result = new StringBuffer();
		for (byte b : bytes) {
		    result.append(String.format("%02x", b));
		}
		return result.toString();
	}

	public void prepForCreate() {
		this.creation = new Date();
		if (salt == null)
			createSalt();
	}
	
	public void createSalt() {
		if (salt != null)
			return;
		//generate secure random salt
		SecureRandom rand = new SecureRandom();
		byte[] randBytes = new byte[16];
		rand.nextBytes(randBytes);
		this.salt = bytesToHexString(randBytes);
		//set salted password Hash if needed
		if (passwordHash != null)
			saltedPasswordHash = saltPasswordHash(passwordHash, salt);
	}
}
