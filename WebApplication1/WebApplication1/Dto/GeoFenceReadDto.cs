namespace WebApplication1.Dto
{
    public class GeoFenceReadDto
    {
        public int Id { get; set; }
        public double EventRadius { get; set; }
        public double TeacherRadius { get; set; }
        public double PairDistance { get; set; }
        public double Latitude { get; set; }
        public double Longitude { get; set; }
    }
}
