namespace WebApplication1.Dto
{
    public class EventCreateDto
    {
        public string? EventName { get; set; }
        public DateOnly? EventDate { get; set; }
        public string? EventLocation { get; set; }
        public bool Status { get; set; }
        public int Teacherid { get; set; }
        public int? Geofenceid { get; set; }
    }
}
