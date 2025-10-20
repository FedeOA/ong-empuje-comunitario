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
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.ong.empuje.comunitario.web_services.dto.OrganizationDTO;
import com.ong.empuje.comunitario.web_services.dto.PresidentDTO;

@Service
public class SoapClientService {
    
    private static final Logger logger = LoggerFactory.getLogger(SoapClientService.class);
    private final RestTemplate restTemplate;
    private static final String SOAP_URL = "https://soap-app-latest.onrender.com/";

    public SoapClientService(RestTemplate restTemplate){
        this.restTemplate = restTemplate;
    }

    //datos por ONG id
    public List<OrganizationDTO> getOrganizations(List<Integer> orgIds){
        try {
            String soapRequest = buildOrganizationsSoapRequest(orgIds);
            logger.debug("SOAP Request Organizations: {}", soapRequest);

            String soapResponse = sendSoapRequest(soapRequest,"list_associations");
            logger.debug("SOAP Response Organizations: {}", soapResponse);

            return parseOrganizationsResponse(soapResponse);
        } catch (Exception e) {
            logger.error("Error consultando organizaciones SOAP: {}", e.getMessage(),e);
            throw new RuntimeException("Error consultando organizaciones SOAP: "+ e.getMessage(),e);
        }
    }

    //datos de presidentes
    public List<PresidentDTO> getPresidents(List<Integer> orgIds){
        try {
            String soapRequest = buildPresidentsSoapRequest(orgIds);
            logger.debug("SOAP Request Presidents: {}", soapRequest);

            String soapResponse = sendSoapRequest(soapRequest, "list_associations");
            logger.debug("SOAP Response Presidents: {}", soapResponse);

            return parsePresidentsResponse(soapResponse);
        } catch (Exception e) {
            logger.error("Error consultando presidentes SOAP: {}", e.getMessage(),e);
            throw new RuntimeException("Error consultando presidentes SOAP: " + e.getMessage(),e);
        }
    }


    private String sendSoapRequest(String soapRequest, String soapAction){
        try{
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_XML);
            headers.set("SOAPAction", soapAction);

            HttpEntity<String> request = new HttpEntity<>(soapRequest, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                SOAP_URL,
                HttpMethod.POST,
                request,
                String.class
            );

            if(response.getStatusCode().is2xxSuccessful()){
                return response.getBody();
            }else{
                throw new RuntimeException("Error en respuesta SOAP: "+response.getStatusCode());
            }

        }catch(Exception e){
            throw new RuntimeException("Error enviando request SOAP: "+ e.getMessage(),e);
        }
    }
    //SOAP REQUEST
    private String buildOrganizationsSoapRequest(List<Integer> orgIds){
        StringBuilder soapRequest = new StringBuilder();
        soapRequest.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
                .append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" ")
                .append("xmlns:auth=\"auth.headers\" ")
                .append("xmlns:tns=\"soap.backend\">")
                .append("<soapenv:Header>")
                .append("<auth:Auth>")
                .append("<auth:Grupo>GrupoA-TM</auth:Grupo>")
                .append("<auth:Clave>clave-tm-a</auth:Clave>")
                .append("</auth:Auth>")
                .append("</soapenv:Header>")
                .append("<soapenv:Body>")
                .append("<tns:list_associations>")
                .append("<tns:org_ids>");

        for(Integer orgId : orgIds){
            soapRequest.append("<tns:string>").append(orgId).append("</tns:string");
        }

        soapRequest.append("</tns:org_ids>")
                .append("</tns:list_associations>")
                .append("</soapenv:Body>")
                .append("</soapenv:Envelope>");

        return soapRequest.toString();
    }

    private String buildPresidentsSoapRequest(List<Integer> orgIds){
        StringBuilder soapRequest = new StringBuilder();
        soapRequest.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
                .append("<soapenv:Envelope xmlns:soapenv=\\\"http://schemas.xmlsoap.org/soap/envelope/\" ")
                .append("xmlns:auth=\"auth.headers\" ")
                .append("xmlns:tns=\"soap.backend\">")
                .append("<soapenv:Header>")
                .append("<auth:Auth>")
                .append("<auth:Grupo>GrupoA-TM</auth:Grupo>")
                .append("<auth:Clave>clave-tm-a</auth:Clave>")
                .append("</auth:Auth>")
                .append("</soapenv:Header>")
                .append("<soapenv:Body>")
                .append("<tns:list_presidents>")
                .append("<tns:org_ids>");

        for(Integer orgId : orgIds){
            soapRequest.append("<tns:string>").append(orgId).append("</tns:string");
        }

        soapRequest.append("</tns:org_ids>")
                .append("</tns:list_presidents>")
                .append("</soapenv:Body>")
                .append("</soapenv:Envelope>");

        return soapRequest.toString();
    }
    
    //PARSE RESPONSE
    private List<OrganizationDTO> parseOrganizationsResponse(String soapResponse) throws Exception{
        List<OrganizationDTO> organizations = new ArrayList<>();
        int i;

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new ByteArrayInputStream(soapResponse.getBytes()));
        
        NodeList orgNodes = document.getElementsByTagNameNS("models", "OrganizationType");

        for(i = 0; i<orgNodes.getLength();i++){
            Element orgElement = (Element) orgNodes.item(i);
            OrganizationDTO org = new OrganizationDTO();

            org.setId(Integer.parseInt(getElementText(orgElement, "id")));
            org.setName(getElementText(orgElement,"name"));
            org.setAddress(getElementText(orgElement, "address"));
            org.setPhone(getElementText(orgElement,"phone"));

            organizations.add(org);
        }
        return organizations;
    }

    private List<PresidentDTO> parsePresidentsResponse(String soapResponse)throws Exception{
        List<PresidentDTO> presidents = new ArrayList<>();
        int i;

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new ByteArrayInputStream(soapResponse.getBytes()));
        
        NodeList presidentNodes = document.getElementsByTagNameNS("models", "PresidentType");

        for(i = 0; i<presidentNodes.getLength();i++){
            Element presidentElement = (Element) presidentNodes.item(i);
            PresidentDTO president = new PresidentDTO();

            president.setId(Integer.parseInt(getElementText(presidentElement, "id")));
            president.setName(getElementText(presidentElement,"name"));
            president.setAddress(getElementText(presidentElement, "address"));
            president.setPhone(getElementText(presidentElement,"phone"));
            president.setOrganizationId(Integer.parseInt(getElementText(presidentElement, "organization_id")));

            presidents.add(president);
        }
        return presidents;
    }


    private String getElementText(Element parent, String tagName){
        NodeList nodes = parent.getElementsByTagNameNS("models", tagName);
        if(nodes.getLength()>0){
            return nodes.item(0).getTextContent();
        }
        return "";
    }
}
