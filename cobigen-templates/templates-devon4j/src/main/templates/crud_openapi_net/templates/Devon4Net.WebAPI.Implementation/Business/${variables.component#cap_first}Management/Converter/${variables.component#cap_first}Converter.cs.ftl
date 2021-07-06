using System;
using Devon4Net.WebAPI.Implementation.Domain.Entities;
using Devon4Net.WebAPI.Implementation.Business.${variables.entityName?cap_first}Management.Dto;
using System.Collections.Generic;
<#list model.properties as property>
<#if property.isCollection>
using Devon4Net.WebAPI.Implementation.Business.<#if property.isEntity>${property.type}</#if>Management.Converter;
</#if>
 </#list>

namespace Devon4Net.WebAPI.Implementation.Business.${variables.entityName?cap_first}Management.Converter
{
    /// <summary>
    /// ${variables.entityName?cap_first}Converter
    /// </summary>
    public static class ${variables.entityName?cap_first}Converter
    {
        /// <summary>
        /// ModelToDto ${variables.entityName?cap_first} transformation
        /// </summary>
        /// <param name="item"></param>
        /// <returns></returns>
        public static ${variables.entityName?cap_first}ResponseDto ModelToDto(${variables.entityName?cap_first} item)
        {
            if(item == null) return new ${variables.entityName?cap_first}ResponseDto();

            return new ${variables.entityName?cap_first}ResponseDto
            {
                ${variables.entityName}Id = item.${variables.entityName}Id,
                <#list model.properties as property>
                <#if property.isCollection>
                <#if property.isEntity>${property.name?cap_first} = ${property.type}Converter.to${property.type}ResponseDto(item.${property.name?cap_first})<#else>${property.name?cap_first} = item.${property.name?cap_first}</#if><#if property?is_last><#else>,</#if>
                <#else>
                ${property.name?cap_first} = item.${property.name?cap_first}<#if property?is_last><#else>,</#if>
                 </#if>
                </#list>
            };
        }

        public static ${variables.entityName?cap_first}ResponseDto[] to${variables.entityName?cap_first}ResponseDto(List<${variables.entityName?cap_first}> items)
        {
           if (items != null) {
            var aux = new ${variables.entityName?cap_first}ResponseDto[items.Count];
            for (int i = 0; i < items.Count; i++) {
                aux[i] = ModelToDto(items[i]);
            }
            return aux;
            } else {return new ${variables.entityName?cap_first}ResponseDto[0];}


        }

        public static List<${variables.entityName?cap_first}> DtoToModel(${variables.entityName?cap_first}Dto[] item)
        {
            var aux = new List<${variables.entityName?cap_first}>();
            Console.WriteLine("item = {0}\n",item.Length);
            for(int i = 0; i < item.Length; i++) {
               if(item[i] == null) { aux[i] = null; }
                else {
                    ${variables.entityName?cap_first} a = new ${variables.entityName?cap_first} {
                     ${variables.entityName?cap_first}Example = item[i].${variables.entityName?cap_first}Example,
                    };
                    aux.Add(a);
                }
            }
            return aux;
        }

   }
}