namespace WebApplication1.Dto
{
    public class NotificationCreateDto
    {
        public int Userid { get; set; }
        public int Eventid { get; set; }
        public string? NotificationDescription { get; set; }
        public DateTime NotificationTimestamp { get; set; }
        public bool Sent { get; set; }
    }
}
