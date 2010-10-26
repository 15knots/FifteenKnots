<?xml version="1.0" encoding="UTF-8"?>
<kml xmlns="http://www.opengis.net/kml/2.2">
<Document>
<#--  <Document xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://earth.google.com/kml/2.1   http://code.google.com/apis/kml/schema/kml21.xsd">
    -->
    <!-- ${.template_name} -->
    <name>GPS device</name>
    <open>1</open>
<#--
    <Snippet maxLines="2">Created Sat Oct 11 22:23:37 2008
    </Snippet>
    <LookAt>
      <longitude>13.31113650000001</longitude>
      <latitude>52.51715049999999</latitude>
      <altitude>0</altitude>
      <tilt>0</tilt>
      <altitudeMode>relativeToGround</altitudeMode>
      <range>2389.426578274666</range>
      <heading>306.45</heading>
    </LookAt>
-->
    <Style id="track_h">
      <IconStyle>
        <color>ffffaaaa</color>
        <scale>1.2</scale>
        <Icon>
          <href>http://maps.google.com/mapfiles/kml/shapes/open-diamond.png</href>
        </Icon>
      </IconStyle>
    </Style>
    <Style id="track_n">
      <IconStyle>
        <color>ffffaaaa</color>
        <Icon>
          <href>http://maps.google.com/mapfiles/kml/shapes/open-diamond.png</href>
        </Icon>
      </IconStyle>
    </Style>
    <StyleMap id="track">
      <Pair>
        <key>normal</key>
        <styleUrl>#track_n</styleUrl>
      </Pair>
      <Pair>
        <key>highlight</key>
        <styleUrl>#track_h</styleUrl>
      </Pair>
    </StyleMap>
    <#-- styles for speed to color encoding -->
    <#list race.speedEncoding.ranges as range>
    <Style id="speed_${range_index?c}">
    <#assign color = toABGRhex(range.color.RGB)>
      <LineStyle>
        <color>${color}</color>
      </LineStyle>
      <#--
      <ListStyle>
        <bgColor>${color}</bgColor>
      </ListStyle>
      -->
    </Style>
    </#list>
    
    <Style id="lineStyle1">
      <LineStyle>
        <color>ffffffff</color>
      </LineStyle>
    </Style>

    <Folder>
    <name>Speed in knots</name>
    <#-- legend for speed to color encoding -->
    <#list race.speedEncoding.ranges as range>
    <ScreenOverlay>
      <name>${range.lowerLimit?string("0.##")} .. ${range.upperLimit?string("0.##")}</name>
	<color>${toABGRhex(range.color.RGB)}</color>
	<overlayXY x="0" y="0" xunits="pixels" yunits="pixels"/>
  	<screenXY y="10" x="${10+range_index*5}" yunits="pixels" xunits="pixels"/>
  	<size y="25" x="4" xunits="pixels" yunits="pixels"/>
    </ScreenOverlay>
    </#list>
    </Folder>

<#if (race.cruises?size> 1)>
    <Folder>
      <name>Boats</name>
</#if>
      <#list race.cruises as cruise>
      <Folder>
        <name><#if cruise.boat.name??>${cruise.boat.name}<#else>Boat #${cruise.boat.index}</#if></name>
        <Folder>
          <name>Trackpoints</name>
          <#list cruise.trackpoints as point>
          <Placemark>
            <name>${point_index}<#if point.speed??> [${point.speed} kts]</#if></name>
            <styleUrl>#track</styleUrl>
<#--        <Snippet maxLines="2"></Snippet>
            <description></description>
-->         <Point><coordinates>${point.position.longitude?c},${point.position.latitude?c}</coordinates></Point>
            <LookAt>
              <longitude>${point.position.longitude?c}</longitude>
              <latitude>${point.position.latitude?c}</latitude>
              <range>100</range>
            </LookAt>
            <TimeStamp>
              <when>${millisToDate(point.date)?datetime?string("yyyy-MM-dd'T'HH:mm:ss'Z'")}</when>
            </TimeStamp>
          </Placemark>
          </#list> <#-- trackpoints -->
        </Folder>
        <Folder>
          <name>Cruise</name>
          <#list cruise.polyLines as line>
          <Placemark>
            <name>${line_index} (Sp ${line.colorIndex})</name>
            <styleUrl>#speed_${line.colorIndex?c}</styleUrl>
            <LineString>
              <tessellate>1</tessellate>
              <coordinates>
<#list line.segments as segment>${segment.position.longitude?c},${segment.position.latitude?c}
</#list> <#-- segment -->
              </coordinates>
            </LineString>
          </Placemark>
          </#list> <#-- polylines -->
        </Folder>
      </#list> <#-- cruises -->
<#if (race.cruises?size> 1)>
      </Folder>
</#if>
    </Folder>
<#if outline??>    
      <Placemark>
      <name>Outline</name>
      <styleUrl>lineStyle1</styleUrl>
      <LineString>
        <tessellate>1</tessellate>
        <coordinates>
        <#list outline as point>${point.longitude?c},${point.latitude?c}
        </#list>
        </coordinates>
      </LineString>
      </Placemark>
</#if>
  </Document>
</kml>
