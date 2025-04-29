namespace WebApplication1.Dto
{
    public class EventReadDto
    {
        public int Id { get; set; }
        public string EventName { get; set; } = null!;
        public DateOnly EventDate { get; set; }
        public string EventLocation { get; set; } = null!;
        public bool Status { get; set; }
        public int Teacherid { get; set; }
        public int Geofenceid { get; set; }
    }
}
