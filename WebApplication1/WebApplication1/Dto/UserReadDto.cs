namespace WebApplication1.Dto
{
    public class UserReadDto
    {
        public int Id { get; set; }
        public string FirstName { get; set; } = null!;
        public string LastName { get; set; } = null!;
        public string UserType { get; set; } = null!;
    }
}
