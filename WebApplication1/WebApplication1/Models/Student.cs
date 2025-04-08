using System.ComponentModel.DataAnnotations;

namespace WebApplication1.Models
{
    public class Student
    {
        [Key]
        public int ID { get; set; }
        public String Name { get; set; }
    }
}
