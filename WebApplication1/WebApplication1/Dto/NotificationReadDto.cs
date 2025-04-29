namespace WebApplication1.Dto
{
    public class NotificationReadDto
    {
        public int Id { get; set; }
        public int Userid { get; set; }
        public int Eventid { get; set; }
        public string NotificationDescription { get; set; } = null!;
        public DateTime NotificationTimestamp { get; set; }
        public bool Sent { get; set; }
    }
}
