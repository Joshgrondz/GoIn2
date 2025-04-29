namespace WebApplication1.Dto
{
    public class LocationReadDto
    {
        public int Id { get; set; }
        public int Userid { get; set; }
        public double Latitude { get; set; }
        public double Longitude { get; set; }
        public double LocAccuracy { get; set; }
        public double LocAltitude { get; set; }
        public double LocSpeed { get; set; }
        public double LocBearing { get; set; }
        public string LocProvider { get; set; } = null!;
        public long TimestampMs { get; set; }
    }
}
