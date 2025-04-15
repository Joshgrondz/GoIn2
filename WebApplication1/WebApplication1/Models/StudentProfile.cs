using System;
using System.Collections.Generic;

namespace WebApplication1.Models;

public partial class StudentProfile
{
    public int Id { get; set; }

    public string? GradeLevel { get; set; }

    public virtual ICollection<ClassRoster> ClassRosters { get; set; } = new List<ClassRoster>();

    public virtual User IdNavigation { get; set; } = null!;

    public virtual ICollection<Pair> PairStudent1s { get; set; } = new List<Pair>();

    public virtual ICollection<Pair> PairStudent2s { get; set; } = new List<Pair>();
}
