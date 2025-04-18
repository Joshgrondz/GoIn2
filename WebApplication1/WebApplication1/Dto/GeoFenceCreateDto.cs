namespace WebApplication1.Dto
{
    public class GeoFenceCreateDto
    {
        public int EventRadius { get; set; }
        public int TeacherRadius { get; set; }
        public double PairDistance { get; set; }
        public double Latitude { get; set; }
        public double Longitude { get; set; }
    }
}
