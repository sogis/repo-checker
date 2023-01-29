<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="3.0"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs">
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
                                    <span>&#160;</span>
                                    <span>&#160;</span>
                                    <xsl:element name="a">
                                        <xsl:attribute name="target">
                                            <xsl:text>_blank</xsl:text>
                                        </xsl:attribute>
                                        <xsl:attribute name="class">
                                            <xsl:text>black-link</xsl:text>
                                        </xsl:attribute>
                                        <xsl:attribute name="href"><xsl:value-of select="endpoint"/></xsl:attribute>
                                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-box-arrow-up-right" viewBox="0 0 16 16">
                                            <rect width="16" height="16" style="fill:white;stroke:none"></rect>
                                            <path fill-rule="evenodd" d="M8.636 3.5a.5.5 0 0 0-.5-.5H1.5A1.5 1.5 0 0 0 0 4.5v10A1.5 1.5 0 0 0 1.5 16h10a1.5 1.5 0 0 0 1.5-1.5V7.864a.5.5 0 0 0-1 0V14.5a.5.5 0 0 1-.5.5h-10a.5.5 0 0 1-.5-.5v-10a.5.5 0 0 1 .5-.5h6.636a.5.5 0 0 0 .5-.5z"></path>
                                            <path fill-rule="evenodd" d="M16 .5a.5.5 0 0 0-.5-.5h-5a.5.5 0 0 0 0 1h3.793L6.146 9.146a.5.5 0 1 0 .708.708L15 1.707V5.5a.5.5 0 0 0 1 0v-5z"></path>
                                        </svg>
                                    </xsl:element>                                    
                                </td>
                                <td>
                                    <xsl:choose>        
                                        <xsl:when test="xs:boolean(checks/check[type = 'ILISITE_XML']/success)">Print something</xsl:when>
                                        <xsl:otherwise>
                                            fuuuubar
                                        </xsl:otherwise>
                                    </xsl:choose>        
                                </td>   
                                <td>
                                    <xsl:choose>        
                                        <xsl:when test="xs:boolean(checks/check[type = 'ILIMODELS_XML']/success)">
                                            Print something
                                        </xsl:when>
                                        <xsl:otherwise>
                                            fuuuubar
                                        </xsl:otherwise>
                                    </xsl:choose>        
                                </td>   
                                <td>
                                    <xsl:choose>        
                                        <xsl:when test="xs:boolean(checks/check[type = 'MODELS']/success)">
                                            <span>
                                                <span class="badge-success">Success</span>
                                            </span>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <span>
                                                <span class="badge-fail">Failure</span>
                                            </span> 
                                            <span>&#160;</span>
                                            <span>&#160;</span>
                                            
                                            <xsl:element name="a">
                                                <xsl:attribute name="target">
                                                    <xsl:text>_blank</xsl:text>
                                                </xsl:attribute>
                                                <xsl:attribute name="class">
                                                    <xsl:text>black-link</xsl:text>
                                                </xsl:attribute>
                                                <xsl:attribute name="href">
                                                    <xsl:variable name="seq" select="tokenize(checks/check[type = 'MODELS']/logfile,'/')"/>
                                                    <xsl:value-of select="$seq[count($seq)-1]"/>
                                                    <xsl:text>/</xsl:text>
                                                    <xsl:value-of select="$seq[count($seq)]"/>
                                                </xsl:attribute>
                                                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-box-arrow-up-right" viewBox="0 0 16 16">
                                                    <rect width="16" height="16" style="fill:white;stroke:none"></rect>
                                                    <path fill-rule="evenodd" d="M8.636 3.5a.5.5 0 0 0-.5-.5H1.5A1.5 1.5 0 0 0 0 4.5v10A1.5 1.5 0 0 0 1.5 16h10a1.5 1.5 0 0 0 1.5-1.5V7.864a.5.5 0 0 0-1 0V14.5a.5.5 0 0 1-.5.5h-10a.5.5 0 0 1-.5-.5v-10a.5.5 0 0 1 .5-.5h6.636a.5.5 0 0 0 .5-.5z"></path>
                                                    <path fill-rule="evenodd" d="M16 .5a.5.5 0 0 0-.5-.5h-5a.5.5 0 0 0 0 1h3.793L6.146 9.146a.5.5 0 1 0 .708.708L15 1.707V5.5a.5.5 0 0 0 1 0v-5z"></path>
                                                </svg>
                                            </xsl:element>                                    
                                        </xsl:otherwise>
                                    </xsl:choose>        
                                </td>   
                                <td>
                                    <xsl:value-of select="format-dateTime(checks/check[type = 'MODELS']/lastUpdate,'[Y0001]-[M01]-[M01] [H]:[m]')"/>
                                </td>   

                            </tr>
                        </xsl:for-each>
                        
                    </tbody>

                </table>
            </div>
        </body>
        </html>
    </xsl:template>
</xsl:stylesheet>