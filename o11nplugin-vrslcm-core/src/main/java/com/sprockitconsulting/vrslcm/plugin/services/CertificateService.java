package com.sprockitconsulting.vrslcm.plugin.services;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sprockitconsulting.vrslcm.plugin.dao.DaoCertificate;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Certificate;
import com.sprockitconsulting.vrslcm.plugin.scriptable.CertificateInfo;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Connection;
/**
 * This service governs access to the LCM Certificate resources and related objects.
 * 
 * @author justin
 */
@Service
public class CertificateService extends AbstractService {
	// Enable Logging
	private static final Logger log = LoggerFactory.getLogger(CertificateService.class);
	
	@Autowired
	private DaoCertificate dao;
	
	public CertificateService() {
		log.debug("CertificateService initialized");
	}
	
	public Certificate getCertificateByValue(Connection connection, String value) {
		return dao.findById(connection, value);
	}
	
	public List<Certificate> getCertificatesByAliases(Connection connection, String[] aliases) {
		return dao.getAllCertificatesMatchingAliases(connection, aliases);
	}

	public List<Certificate> getAllCertificates(Connection connection) {
		return dao.findAll(connection);
	}

	public Certificate createCertificate(Connection connection, CertificateInfo certificateInfo) {
		return dao.createCertificate(connection, certificateInfo);
	}
	
	public String createCertificateSigningRequest(Connection connection, CertificateInfo certificateInfo) {
		return dao.createCertificateRequest(connection, certificateInfo);
	}
	
	public Certificate importCertificate(Connection connection, String name, String passphrase, String certificateChain, String privateKey) {
		return dao.importSignedCertificate(connection, name, passphrase, certificateChain, privateKey);
	}
	
	public void deleteCertificate(Connection connection, Certificate certificate) {
		dao.delete(connection, certificate);
	}

}
