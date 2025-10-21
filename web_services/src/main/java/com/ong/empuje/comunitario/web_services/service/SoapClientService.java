package com.ong.empuje.comunitario.web_services.service;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.ong.empuje.comunitario.web_services.dto.in.OrganizationDTO;
import com.ong.empuje.comunitario.web_services.dto.in.PresidentDTO;

@Service
public class SoapClientService {

    private static final Logger logger = LoggerFactory.getLogger(SoapClientService.class);
    private final RestTemplate restTemplate;
    private static final String SOAP_URL = "https://soap-app-latest.onrender.com/";

    public SoapClientService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<OrganizationDTO> getOrganizations(List<Integer> orgIds) {
        try {
            String soapRequest = buildOrganizationsSoapRequest(orgIds);
            logger.debug("SOAP Request Organizations: {}", soapRequest);
            String soapResponse = sendSoapRequest(soapRequest, "list_associations");
            logger.debug("SOAP Response Organizations: {}", soapResponse);
            List<OrganizationDTO> organizations = parseOrganizationsResponse(soapResponse);
            logger.debug("Returning organizations: {}", organizations);
            return organizations;
        } catch (Exception e) {
            logger.error("Error consulting organizations SOAP: {}", e.getMessage(), e);
            throw new RuntimeException("Error consulting organizations: " + e.getMessage(), e);
        }
    }

    public List<PresidentDTO> getPresidents(List<Integer> orgIds) {
        try {
            String soapRequest = buildPresidentsSoapRequest(orgIds);
            logger.debug("SOAP Request Presidents: {}", soapRequest);
            String soapResponse = sendSoapRequest(soapRequest, "list_presidents");
            logger.debug("SOAP Response Presidents: {}", soapResponse);
            List<PresidentDTO> presidents = parsePresidentsResponse(soapResponse);
            logger.debug("Returning presidents: {}", presidents);
            return presidents;
        } catch (Exception e) {
            logger.error("Error consulting presidents SOAP: {}", e.getMessage(), e);
            throw new RuntimeException("Error consulting presidents: " + e.getMessage(), e);
        }
    }

    private String sendSoapRequest(String soapRequest, String soapAction) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_XML);
            headers.set("SOAPAction", soapAction);
            HttpEntity<String> request = new HttpEntity<>(soapRequest, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    SOAP_URL, HttpMethod.POST, request, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            }
            String errorMessage = "SOAP request failed: Status=" + response.getStatusCode();
            if (response.getBody() != null) {
                errorMessage += ", Response=" + response.getBody();
            }
            throw new RuntimeException(errorMessage);
        } catch (HttpServerErrorException e) {
            logger.error("SOAP error: Status={}, Response={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("SOAP request failed: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            logger.error("Unexpected SOAP error: {}", e.getMessage(), e);
            throw new RuntimeException("SOAP request failed: " + e.getMessage(), e);
        }
    }

    private String buildOrganizationsSoapRequest(List<Integer> orgIds) {
        StringBuilder soapRequest = new StringBuilder();
        soapRequest.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
                .append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" ")
                .append("xmlns:auth=\"auth.headers\" xmlns:tns=\"soap.backend\">")
                .append("<soapenv:Header>")
                .append("<auth:Auth>")
                .append("<auth:Grupo>GrupoA-TM</auth:Grupo>")
                .append("<auth:Clave>clave-tm-a</auth:Clave>")
                .append("</auth:Auth>")
                .append("</soapenv:Header>")
                .append("<soapenv:Body>")
                .append("<tns:list_associations>")
                .append("<tns:org_ids>");
        for (Integer orgId : orgIds) {
            soapRequest.append("<tns:string>").append(orgId).append("</tns:string>");
        }
        soapRequest.append("</tns:org_ids>")
                .append("</tns:list_associations>")
                .append("</soapenv:Body>")
                .append("</soapenv:Envelope>");
        return soapRequest.toString();
    }

    private String buildPresidentsSoapRequest(List<Integer> orgIds) {
        StringBuilder soapRequest = new StringBuilder();
        soapRequest.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
                .append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" ")
                .append("xmlns:auth=\"auth.headers\" xmlns:tns=\"soap.backend\">")
                .append("<soapenv:Header>")
                .append("<auth:Auth>")
                .append("<auth:Grupo>GrupoA-TM</auth:Grupo>")
                .append("<auth:Clave>clave-tm-a</auth:Clave>")
                .append("</auth:Auth>")
                .append("</soapenv:Header>")
                .append("<soapenv:Body>")
                .append("<tns:list_presidents>")
                .append("<tns:org_ids>");
        for (Integer orgId : orgIds) {
            soapRequest.append("<tns:string>").append(orgId).append("</tns:string>");
        }
        soapRequest.append("</tns:org_ids>")
                .append("</tns:list_presidents>")
                .append("</soapenv:Body>")
                .append("</soapenv:Envelope>");
        return soapRequest.toString();
    }

    private List<OrganizationDTO> parseOrganizationsResponse(String soapResponse) throws Exception {
        List<OrganizationDTO> organizations = new ArrayList<>();
        if (soapResponse == null || soapResponse.trim().isEmpty()) {
            logger.warn("Empty SOAP response for organizations");
            return organizations;
        }
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new ByteArrayInputStream(soapResponse.getBytes("UTF-8")));
        document.getDocumentElement().normalize();
        NodeList orgNodes = document.getElementsByTagNameNS("*", "OrganizationType");
        if (orgNodes.getLength() == 0) {
            orgNodes = document.getElementsByTagName("OrganizationType");
            logger.debug("Using local name OrganizationType, found {} nodes", orgNodes.getLength());
        }
        for (int i = 0; i < orgNodes.getLength(); i++) {
            Element orgElement = (Element) orgNodes.item(i);
            OrganizationDTO org = new OrganizationDTO();
            org.setId(Integer.parseInt(getElementText(orgElement, "id")));
            org.setName(getElementText(orgElement, "name"));
            org.setAddress(getElementText(orgElement, "address"));
            org.setPhone(getElementText(orgElement, "phone"));
            logger.debug("Parsed organization: id={}, name={}, address={}, phone={}", org.getId(), org.getName(), org.getAddress(), org.getPhone());
            organizations.add(org);
        }
        return organizations;
    }

    private List<PresidentDTO> parsePresidentsResponse(String soapResponse) throws Exception {
        List<PresidentDTO> presidents = new ArrayList<>();
        if (soapResponse == null || soapResponse.trim().isEmpty()) {
            logger.warn("Empty SOAP response for presidents");
            return presidents;
        }
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new ByteArrayInputStream(soapResponse.getBytes("UTF-8")));
        document.getDocumentElement().normalize();
        NodeList presidentNodes = document.getElementsByTagNameNS("*", "PresidentType");
        if (presidentNodes.getLength() == 0) {
            presidentNodes = document.getElementsByTagName("PresidentType");
            logger.debug("Using local name PresidentType, found {} nodes", presidentNodes.getLength());
        }
        for (int i = 0; i < presidentNodes.getLength(); i++) {
            Element presidentElement = (Element) presidentNodes.item(i);
            PresidentDTO president = new PresidentDTO();
            president.setId(Integer.parseInt(getElementText(presidentElement, "id")));
            president.setFirstName(getElementText(presidentElement, "name"));
            president.setPhone(getElementText(presidentElement, "phone"));
            president.setOrganizationId(Integer.parseInt(getElementText(presidentElement, "organization_id")));
            logger.debug("Parsed president: id={}, name={}, address={}, phone={}, organizationId={}", 
                president.getId(), president.getFirstName(), president.getPhone(), president.getOrganizationId());
            presidents.add(president);
        }
        return presidents;
    }

    private String getElementText(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagNameNS("*", tagName);
        if (nodes.getLength() == 0) {
            nodes = parent.getElementsByTagName(tagName);
        }
        return nodes.getLength() > 0 ? nodes.item(0).getTextContent() : "";
    }
}