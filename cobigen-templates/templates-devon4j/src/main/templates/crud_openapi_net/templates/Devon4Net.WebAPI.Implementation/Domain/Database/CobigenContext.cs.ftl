using Devon4Net.WebAPI.Implementation.Domain.Entities;
using Microsoft.EntityFrameworkCore;

namespace Devon4Net.WebAPI.Implementation.Domain.Database
{
    /// <summary>
    /// Cobigen database context definition
    /// </summary>
    public class CobigenContext : DbContext
    {
        /// <summary>
        /// Cobigen context definition
        /// </summary>
        /// <param name="options"></param>
        public CobigenContext(DbContextOptions<CobigenContext> options)
            : base(options)
        {
        }

        /// <summary>
        /// Any extra configuration should be here
        /// </summary>
        /// <param name="optionsBuilder"></param>
        protected override void OnConfiguring(DbContextOptionsBuilder optionsBuilder)
        {
        }

        /// <summary>
        /// Model rules definition
        /// </summary>
        /// <param name="modelBuilder"></param>
        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            <#list model.allEntityDefs as entity>

            modelBuilder.Entity<${entity.name?cap_first}>(entity =>
            {
                <#list entity.properties as property>
                    <#if property.isCollection>

                entity.HasMany(e => e.${property.name?cap_first});
                    <#else>

                entity.Property(e => e.${property.name?cap_first})
                    .IsRequired()
                    .HasMaxLength(255);
                    </#if>
                </#list>

                entity.Property(e => e.${entity.name?cap_first}Id)
                .IsRequired()
                .HasMaxLength(255);
            });
            </#list>
        }
}
}
