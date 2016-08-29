
    <profiles>
      <profile>
         <id>all</id>
         <activation>
            <activeByDefault>false</activeByDefault>
         </activation>
         <modules>
<#list modules as module>
            <module>${module}</module>
</#list>
         </modules>
      </profile>
    </profiles>

