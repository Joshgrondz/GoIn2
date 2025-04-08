namespace WebApplication1.Database
{
    using Microsoft.EntityFrameworkCore;
    using System.Numerics;
    using WebApplication1.Models;

    public class TableContext : DbContext
    {
        public TableContext(DbContextOptions<TableContext> options) : base(options) { }

        public DbSet<Student> Student { get; set; }
       
        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            //Set entities to specific table names
            modelBuilder.Entity<Student>().ToTable("Student");
            
        }
    }
}