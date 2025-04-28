namespace WebApplication1.Dto
{
    public class StudentProfileCreateDto
    {
        public int Id { get; set; } // This links to the User.Id
        public string? GradeLevel { get; set; }
    }
}
