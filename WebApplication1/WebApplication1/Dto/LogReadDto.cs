namespace WebApplication1.Dto
{
    public class LogReadDto
    {
        public int Id { get; set; }
        public int Eventid { get; set; }
        public string LogDescription { get; set; } = null!;
        public DateTime Timestamp { get; set; }
    }
}
