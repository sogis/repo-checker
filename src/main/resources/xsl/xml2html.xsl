<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="3.0">
    <xsl:output encoding="UTF-8" indent="yes" method="html"/>
    <xsl:template match="/repositories">
        <html>
        <head>
            <title>INTERLIS Repository Checker</title>
            <meta name="description" content="INTERLIS Repository Checker"/>
            <meta name="keywords" content="INTERLIS, repository, checker, models"/>
            <meta name="author" content="Stefan Ziegler" />
            <meta name="viewport" content="width=device-width, initial-scale=1" />
            <meta http-equiv="cache-control" content="no-cache"/>

            <style>
                html {
                    font-family: Helvetica, Arial, sans-serif;
                    font-size: 16px;
                    color: #333333;
                }

                body {
                    margin: 0px;
                    padding: 0px;
                    background: #FFFFFF; 
                }

                #container {
                    margin-left: auto;
                    margin-right: auto;
                    width: 1400px;
                    max-width: 95%;
                    background-color: #FFFFFF;        
                }

                #title {
                    margin-top: 60px;
                    font-size: 48px;
                    font-weight: 700;
                    text-align: center;
                }

                .styled-table {
                    border-collapse: collapse;
                    margin-top: 60px;
                    margin-left: auto;
                    margin-right: auto;
                    color: #333333;
                    width: 100%;
                    min-width: 400px;
                }
                
                .styled-table table {
                    border-collapse: collapse;
                }
                
                .styled-table thead tr {
                    background-color: #F7F7F7;
                    color: #333333;
                    text-align: left;
                }
                
                .styled-table tbody tr {
                    border-bottom: 1px solid #dddddd;
                }
                
                .styled-table th,
                .styled-table td {
                    padding: 12px 15px;
                }

.badge-fail {
    background-color: #FAA582;
    color: #333333;
    padding: 4px 8px;
    text-align: center;
    border-radius: 5px;
    display: inline-block;
    width: 80px;
}

.badge-success {
    background-color: #92C5DE;
    color: #333333;
    padding: 4px 8px;
    text-align: center;
    border-radius: 5px;
    display: inline-block;
    width: 80px;
}

.summary-fail {
    background-color: #FAA582; 
    border: 1px solid white;
}

.summary-success {
    background-color: #92C5DE; 
    border: 1px solid white;
}
                                
                .fail-cell {
                    background-color: #FAA582;
                }
                
                .success-cell {
                    background-color: #92C5DE;
                }
                                
                .black-link {
                    overflow: hidden;
                    text-overflow: ellipsis;
                    color: #333333; 
                    text-decoration: underline !important;
                }
                
                a.black-link:hover {
                    color: #333333;
                    text-decoration: underline !important;
                }  
                
                a.black-link:visited {
                    color: #333333; 
                    text-decoration: underline !important;
                }  

                @media 
                only screen and (max-width: 760px),
                (min-device-width: 768px) and (max-device-width: 1024px)  {
                
                    /* Force table to not be like tables anymore */
                    table, thead, tbody, th, td, tr { 
                        display: block; 
                    }
                    
                    /* Hide table headers (but not display: none;, for accessibility) */
                    thead tr { 
                        position: absolute;
                        top: -9999px;
                        left: -9999px;
                    }
                    
                    tr { border: 1px solid #dddddd; }
                    
                    td { 
                        /* Behave  like a "row" */
                        border: none;
                        border-bottom: 1px solid #dddddd; 
                        position: relative;
                        padding-left: 50%; 
                    }
                    
                    td:before { 
                        /* Now like a table header */
                        position: absolute;
                        /* Top/left values mimic padding */
                        top: 6px;
                        left: 6px;
                        width: 45%; 
                        padding-right: 10px; 
                        white-space: nowrap;
                    }                
                }
            </style>
        </head>

        <body>
            <div id="container">
                
                <div id="title">
                    INTERLIS Repository Checker
                </div>

                <table class="styled-table">
                    <colgroup>
                        <col span="1" style="width:30%"/>
                        <col span="1" style="width:17%"/>
                        <col span="1" style="width:17%"/>
                        <col span="1" style="width:17%"/>
                        <col span="1" style="width:19%"/>
                    </colgroup>
                    <thead>
                        <tr>
                            <th>Repository</th>
                            <th>ilisite.xml</th>
                            <th>ilimodels.xml</th>
                            <th>Models</th>
                            <th>Last Validation</th>
                        </tr>
                    </thead>
                    <tbody>
                        <xsl:for-each select="repository">
                        <xsl:sort select="endpoint" data-type="text"/> 
                            <tr>
                                <td>
                                    <xsl:value-of select="endpoint"/>
                                </td>
                                <td>
                                    <xsl:value-of select="checks/check[type = 'ILISITE_XML']/success"/>
                                </td>        
                            </tr>
                        </xsl:for-each>
                        
                    </tbody>

                </table>


                <h2>Beschreibung</h2>
                <p>
                    <xsl:value-of disable-output-escaping="yes" select="shortDescription"/>
                    <xsl:if test="furtherInformation">
                        <br/><br/>
                        Weiterführende Informationen: 
                        <xsl:element name="a">
                            <xsl:attribute name="target">
                                <xsl:text>_blank</xsl:text>
                            </xsl:attribute>
                            <xsl:attribute name="href"><xsl:value-of select="furtherInformation"/></xsl:attribute>
                            <xsl:value-of select="furtherInformation"/>
                        </xsl:element>
                    </xsl:if>

                </p>

                <h2>Datum der letzten Publikation</h2>
                <p>
                    <xsl:value-of select="format-date(lastPublishingDate,'[D01].[M01].[Y0001]')"/>
                </p>

                <xsl:if test="model">
                    <h2>Datenmodell</h2>
                    <p>
                        <xsl:element name="a">
                            <xsl:attribute name="target">
                                <xsl:text>_blank</xsl:text>
                            </xsl:attribute>
                            <xsl:attribute name="href">https://geo.so.ch/modelfinder/?expanded=true&amp;query=<xsl:value-of select="model"/></xsl:attribute>
                            <xsl:value-of select="model"/>
                        </xsl:element>
                    </p>
                </xsl:if>

                <h2>Lizenz</h2>
                <p>
                    <xsl:element name="a">
                        <xsl:attribute name="target">
                            <xsl:text>_blank</xsl:text>
                        </xsl:attribute>
                        <xsl:attribute name="href"><xsl:value-of select="licence"/></xsl:attribute>
                        <xsl:value-of select="licence"/>
                    </xsl:element>
                </p>

                <h2>Kontakt (fachlich)</h2>
                <p>
                    <xsl:value-of select="owner/agencyName"/><br/>
                    <xsl:if test="owner/division">
                        <xsl:value-of select="owner/division"/><br/>
                    </xsl:if>
                    Telefon <xsl:value-of select="owner/phone"/><br/>
                    <xsl:element name="a">
                        <xsl:attribute name="href"><xsl:value-of select="owner/email"/></xsl:attribute><xsl:value-of select="substring(owner/email, 8)"/>
                    </xsl:element><br/>
                    <xsl:element name="a">
                        <xsl:attribute name="target">
                            <xsl:text>_blank</xsl:text>
                        </xsl:attribute>
                        <xsl:attribute name="href"><xsl:value-of select="owner/officeAtWeb"/></xsl:attribute>
                        <xsl:value-of select="substring(owner/officeAtWeb, 9)"/>
                    </xsl:element>
                </p>

                <h2>Kontakt (technisch)</h2>
                <p>
                    <xsl:value-of select="servicer/agencyName"/><br/>
                    Telefon <xsl:value-of select="servicer/phone"/><br/>
                    <xsl:element name="a">
                        <xsl:attribute name="href"><xsl:value-of select="servicer/email"/></xsl:attribute><xsl:value-of select="substring(servicer/email, 8)"/>
                    </xsl:element><br/>
                    <xsl:element name="a">
                        <xsl:attribute name="target">
                            <xsl:text>_blank</xsl:text>
                        </xsl:attribute>
                        <xsl:attribute name="href"><xsl:value-of select="servicer/officeAtWeb"/></xsl:attribute>
                        <xsl:value-of select="substring(servicer/officeAtWeb, 9)"/>
                    </xsl:element>
                </p>

                <xsl:if test="services">
                <h2>Dienste / Web GIS Client</h2>
                    <p class="datenebene">
                        <xsl:if test="wgcPreviewLayer">
                            <xsl:variable name="wgcPreviewLayerUrl">https://geo.so.ch/map?l=<xsl:value-of select="wgcPreviewLayer/identifier"/></xsl:variable> 
                            Themenvorschau im <a href="{$wgcPreviewLayerUrl}" target="_blank">Web GIS Client</a>.
                        <br/>
                        <br/>
                        </xsl:if>

                        Tabellen dieses Themas sind in folgenden Diensten als Karten- oder Datenlayer publiziert:

                        <table>
                            <colgroup>
                                <col span="1" style="width: 70%;"/>
                                <col span="1" style="width: 10%;"/>
                                <col span="1" style="width: 10%;"/>
                                <col span="1" style="width: 10%;"/>
                            </colgroup>
                            <thead>
                                <tr>
                                    <th>Karten-/Datenlayer</th>
                                    <th style="text-align:center;"><a href="https://geo.so.ch/map" target="_blank">WGC</a></th>
                                    <th style="text-align:center;"><a href="https://geo.so.ch/api/wms?SERVICE=WMS&amp;REQUEST=GetCapabilities&amp;VERSION=1.3.0" target="_blank">WMS</a></th>
                                    <th style="text-align:center;"><a href="https://geo.so.ch/api/wfs?SERVICE=WFS&amp;VERSION=1.0.0&amp;REQUEST=GetCapabilities" target="_blank">WFS</a></th>
                                    <th style="text-align:center;"><a href="https://geo.so.ch/api/data/v1/api/" target="_blank">Dataservice</a></th>
                                </tr>
                            </thead>
                            <tbody>

                                <xsl:for-each-group select="services/service" group-by="layers/layer/identifier">
                                    <xsl:sort select="current-grouping-key()" data-type="text" order="ascending"/>
                                    <tr>
                                        <td><xsl:value-of select="current-grouping-key()"/></td>

                                        <xsl:comment>https://icons.getbootstrap.com/</xsl:comment>
                                        <td style="text-align:center;">
                                            <xsl:choose>
                                                <xsl:when test="current-group()[type='WGC']">
                                                    <xsl:variable name="wgcUrl">https://geo.so.ch/map?l=<xsl:value-of select="current-grouping-key()"/></xsl:variable> 
                                                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-box-arrow-up-right" viewBox="0 0 16 16">
                                                        <a class="black-link" href="{$wgcUrl}" target="_blank">
                                                            <rect width="16" height="16" style="fill:#FFFFFF00;stroke:none" />
                                                            <path d="M16 8s-3-5.5-8-5.5S0 8 0 8s3 5.5 8 5.5S16 8 16 8zM1.173 8a13.133 13.133 0 0 1 1.66-2.043C4.12 4.668 5.88 3.5 8 3.5c2.12 0 3.879 1.168 5.168 2.457A13.133 13.133 0 0 1 14.828 8c-.058.087-.122.183-.195.288-.335.48-.83 1.12-1.465 1.755C11.879 11.332 10.119 12.5 8 12.5c-2.12 0-3.879-1.168-5.168-2.457A13.134 13.134 0 0 1 1.172 8z"/>
                                                            <path d="M8 5.5a2.5 2.5 0 1 0 0 5 2.5 2.5 0 0 0 0-5zM4.5 8a3.5 3.5 0 1 1 7 0 3.5 3.5 0 0 1-7 0z"/>
                                                        </a>
                                                    </svg>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-x" viewBox="0 0 16 16">
                                                    <path d="M4.646 4.646a.5.5 0 0 1 .708 0L8 7.293l2.646-2.647a.5.5 0 0 1 .708.708L8.707 8l2.647 2.646a.5.5 0 0 1-.708.708L8 8.707l-2.646 2.647a.5.5 0 0 1-.708-.708L7.293 8 4.646 5.354a.5.5 0 0 1 0-.708z"/>
                                                    </svg>
                                                </xsl:otherwise>
                                            </xsl:choose>
                                        </td>
                                        <td style="text-align:center;">
                                            <xsl:choose>
                                                <xsl:when test="current-group()[type='WMS']">
                                                    <xsl:variable name="wgcUrl">https://geo.so.ch/map?l=<xsl:value-of select="current-grouping-key()"/></xsl:variable> 
                                                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-check-lg" viewBox="0 0 16 16">
                                                    <path d="M12.736 3.97a.733.733 0 0 1 1.047 0c.286.289.29.756.01 1.05L7.88 12.01a.733.733 0 0 1-1.065.02L3.217 8.384a.757.757 0 0 1 0-1.06.733.733 0 0 1 1.047 0l3.052 3.093 5.4-6.425a.247.247 0 0 1 .02-.022Z"/>
                                                    </svg>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-x" viewBox="0 0 16 16">
                                                    <path d="M4.646 4.646a.5.5 0 0 1 .708 0L8 7.293l2.646-2.647a.5.5 0 0 1 .708.708L8.707 8l2.647 2.646a.5.5 0 0 1-.708.708L8 8.707l-2.646 2.647a.5.5 0 0 1-.708-.708L7.293 8 4.646 5.354a.5.5 0 0 1 0-.708z"/>
                                                    </svg>
                                                </xsl:otherwise>
                                            </xsl:choose>
                                        </td>
                                        <td style="text-align:center;">
                                            <xsl:choose>
                                                <xsl:when test="current-group()[type='WFS']">
                                                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-check-lg" viewBox="0 0 16 16">
                                                    <path d="M12.736 3.97a.733.733 0 0 1 1.047 0c.286.289.29.756.01 1.05L7.88 12.01a.733.733 0 0 1-1.065.02L3.217 8.384a.757.757 0 0 1 0-1.06.733.733 0 0 1 1.047 0l3.052 3.093 5.4-6.425a.247.247 0 0 1 .02-.022Z"/>
                                                    </svg>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-x" viewBox="0 0 16 16">
                                                    <path d="M4.646 4.646a.5.5 0 0 1 .708 0L8 7.293l2.646-2.647a.5.5 0 0 1 .708.708L8.707 8l2.647 2.646a.5.5 0 0 1-.708.708L8 8.707l-2.646 2.647a.5.5 0 0 1-.708-.708L7.293 8 4.646 5.354a.5.5 0 0 1 0-.708z"/>
                                                    </svg>
                                                </xsl:otherwise>
                                            </xsl:choose>
                                        </td>
                                        <td style="text-align:center;">
                                            <xsl:choose>
                                                <xsl:when test="current-group()[type='DATA']">
                                                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-check-lg" viewBox="0 0 16 16">
                                                    <path d="M12.736 3.97a.733.733 0 0 1 1.047 0c.286.289.29.756.01 1.05L7.88 12.01a.733.733 0 0 1-1.065.02L3.217 8.384a.757.757 0 0 1 0-1.06.733.733 0 0 1 1.047 0l3.052 3.093 5.4-6.425a.247.247 0 0 1 .02-.022Z"/>
                                                    </svg>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-x" viewBox="0 0 16 16">
                                                    <path d="M4.646 4.646a.5.5 0 0 1 .708 0L8 7.293l2.646-2.647a.5.5 0 0 1 .708.708L8.707 8l2.647 2.646a.5.5 0 0 1-.708.708L8 8.707l-2.646 2.647a.5.5 0 0 1-.708-.708L7.293 8 4.646 5.354a.5.5 0 0 1 0-.708z"/>
                                                    </svg>
                                                </xsl:otherwise>
                                            </xsl:choose>
                                        </td>
                                    </tr>
                                </xsl:for-each-group>
                            </tbody>
                        </table>

                        Geobasidaten nach Bundesrecht werden ebenfalls auf <a href="https://geodienste.ch">geodienste.ch</a> publiziert.
                    </p>
                </xsl:if>

                <xsl:if test="tablesInfo/tableInfo">

                    <h2>Inhalt</h2>

                    <xsl:apply-templates select="tablesInfo/tableInfo" /> 

                </xsl:if>

            </div>
        </body>
        </html>
    </xsl:template>

    <xsl:template match="tableInfo">

        <h3><xsl:value-of select="sqlName"/> (<xsl:value-of select="title"/>)</h3>

        <p class="datenebene"><xsl:value-of disable-output-escaping="yes" select="shortDescription"/></p>

            <table>
                <colgroup>
                    <col span="1" style="width: 25%;"/>
                    <col span="1" style="width: 50%;"/>
                    <col span="1" style="width: 13%;"/>
                    <col span="1" style="width: 12%;"/>
                </colgroup>

                <tr style="border-bottom: 1px solid #eee;">
                    <th>
                        Name
                    </th>
                    <th>
                        Beschreibung
                    </th>
                    <th>
                        Datentyp
                    </th>
                    <th>
                        Pflichtattribut
                    </th>
                </tr>

                <xsl:for-each select="attributesInfo/attributeInfo">
                    <xsl:sort select="name"/>
                    <tr>
                        <td>
                            <xsl:value-of select="name"/>
                        </td>
                        <td>
                            <xsl:value-of disable-output-escaping="yes" select="shortDescription"/>
                        </td>
                        <td>
                            <xsl:value-of select="datatype"/>
                        </td>
                        <td>
                            <xsl:if test="mandatory='true'">
                                <xsl:text>ja</xsl:text>
                            </xsl:if>
                            <xsl:if test="mandatory='false'">
                                <xsl:text>nein</xsl:text>
                            </xsl:if>
                        </td>
                    </tr>
                </xsl:for-each>
            </table>

    </xsl:template>

</xsl:stylesheet>