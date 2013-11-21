package models.snipstory;

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
	@Formats.DateTime(pattern="dd/MM/yyyy")
	@Temporal(TemporalType.DATE)
	@Column(nullable = false)
	public Date birthdate;
	
	@Column(length = 32, nullable = false)
	public String salt;
	
	@Column(length = 64)
	public String saltedPasswordHash;
	
	@Temporal(TemporalType.TIMESTAMP)
	public Date lastLogin;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(nullable = false)
	public Date creation = new Date();
	
	@Column(nullable = false)
	public int numLogins = 0;
		
	@Required
	@MaxLength(64)
	@MinLength(64)
	@Transient
	public String passwordHash;
}
