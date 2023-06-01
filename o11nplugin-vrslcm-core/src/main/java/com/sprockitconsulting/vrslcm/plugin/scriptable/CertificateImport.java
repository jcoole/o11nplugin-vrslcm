package com.sprockitconsulting.vrslcm.plugin.scriptable;

/**
 * Simple class to assist with mapping necessary values on Certificate Import to a JSON body.
 * @author justin
 *
 */
public class CertificateImport {

	private String alias;
	private String certificateChain;
	private String passphrase;
	private String privateKey;
	
	public CertificateImport() {
		
	}

	public CertificateImport(String alias, String certificateChain, String passphrase, String privateKey) {
		this.alias = alias;
		this.certificateChain = certificateChain;
		this.passphrase = passphrase;
		this.privateKey = privateKey;
	}

	public String getAlias() {
		return alias;
	}

	public String getCertificateChain() {
		return certificateChain;
	}

	public String getPassphrase() {
		return passphrase;
	}

	public String getPrivateKey() {
		return privateKey;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public void setCertificateChain(String certificateChain) {
		this.certificateChain = certificateChain;
	}

	public void setPassphrase(String passphrase) {
		this.passphrase = passphrase;
	}

	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}
	
	

}
