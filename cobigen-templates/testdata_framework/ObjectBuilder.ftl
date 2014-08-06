<#include '/makros.ftl'>
package ${variables.rootPackage}.common.builders;

import java.util.LinkedList;
import java.util.List;

import ${pojo.package}.${pojo.name};
import ${variables.rootPackage}.common.builders.P;

public class ${pojo.name}Builder {

    private List<P<${pojo.name}>> parameterToBeApplied = new LinkedList<P<${pojo.name}>>();
    
    private void fillMandatoryFields() {
        <#list pojo.attributes as attr>
		<#if (attr.annotations.javax_validation_constraints_NotNull)?has_content>
		<@callNotNullPropertyWithDefaultValue attr=attr/>
		
		</#if>
	    </#list>
    }
    
    <#list pojo.attributes as attr>
	public ${pojo.name}Builder ${attr.name}(final ${attr.type} ${attr.name}) {
        parameterToBeApplied.add(new P<${pojo.name}>() {
            @Override
            public void apply(${pojo.name} target) {
                target.set${attr.name?cap_first}(${attr.name});
            }
        });
        return this;
    }
    </#list>
    
    public ${pojo.name} createNew() {
        ${pojo.name} ${pojo.name?lower_case} = new ${pojo.name}();
        for (P<${pojo.name}> parameter : parameterToBeApplied) {
            parameter.apply(${pojo.name?lower_case});
        }
        return ${pojo.name?lower_case};
    }

}