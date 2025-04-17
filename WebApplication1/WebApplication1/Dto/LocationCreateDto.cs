namespace WebApplication1.Dto
{
    public class LocationCreateDto
    {
        public int Userid { get; set; }
        public double Latitude { get; set; }
        public double Longitude { get; set; }
        public float LocAccuracy { get; set; }
        public float LocAltitude { get; set; }
        public float LocSpeed { get; set; }
        public float LocBearing { get; set; }
        public string LocProvider { get; set; } = string.Empty;
        public long TimestampMs { get; set; }
    }
}
