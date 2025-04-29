using System;
using System.Collections.Generic;
using Microsoft.EntityFrameworkCore;

namespace WebApplication1.Models;

public partial class GoIn2Context : DbContext
{
    public GoIn2Context()
    {
    }

    public GoIn2Context(DbContextOptions<GoIn2Context> options)
        : base(options)
    {
    }

    public virtual DbSet<ActiveEventsView> ActiveEventsViews { get; set; }

    public virtual DbSet<Class> Classes { get; set; }

    public virtual DbSet<ClassEvent> ClassEvents { get; set; }

    public virtual DbSet<ClassRoster> ClassRosters { get; set; }

    public virtual DbSet<Event> Events { get; set; }

    public virtual DbSet<GeoFence> GeoFences { get; set; }

    public virtual DbSet<Location> Locations { get; set; }

    public virtual DbSet<Log> Logs { get; set; }

    public virtual DbSet<LogsForActiveEventsView> LogsForActiveEventsViews { get; set; }

    public virtual DbSet<Message> Messages { get; set; }

    public virtual DbSet<MostRecentStudentLocationView> MostRecentStudentLocationViews { get; set; }

    public virtual DbSet<Notification> Notifications { get; set; }

    public virtual DbSet<Pair> Pairs { get; set; }

    public virtual DbSet<StudentProfile> StudentProfiles { get; set; }

    public virtual DbSet<StudentsInEventsView> StudentsInEventsViews { get; set; }

    public virtual DbSet<TeacherProfile> TeacherProfiles { get; set; }

    public virtual DbSet<User> Users { get; set; }

    protected override void OnConfiguring(DbContextOptionsBuilder optionsBuilder)
    {
        // ⬇️ ADDED this IF statement to protect InMemory usage
        if (!optionsBuilder.IsConfigured)
        {
#warning To protect potentially sensitive information in your connection string, you should move it out of source code. You can avoid scaffolding the connection string by using the Name= syntax to read it from configuration - see https://go.microsoft.com/fwlink/?linkid=2131148. For more guidance on storing connection strings, see https://go.microsoft.com/fwlink/?LinkId=723263.
            optionsBuilder.UseSqlServer("Server=goin2.database.windows.net;Database=GoIn2;User ID=dbadmin;Password=Bilitski!;Encrypt=True;");
        }
        // ⬆️ IF the options are already configured (for example to InMemory), it skips setting up SQL Server
    }

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        modelBuilder.Entity<ActiveEventsView>(entity =>
        {
            entity
                .HasNoKey()
                .ToView("ActiveEventsView");

            entity.Property(e => e.EventDate).HasColumnName("eventDate");
            entity.Property(e => e.EventId).ValueGeneratedOnAdd();
            entity.Property(e => e.EventLocation)
                .HasMaxLength(255)
                .HasColumnName("eventLocation");
            entity.Property(e => e.EventName)
                .HasMaxLength(100)
                .HasColumnName("eventName");
            entity.Property(e => e.Geofenceid).HasColumnName("geofenceid");
            entity.Property(e => e.Teacherid).HasColumnName("teacherid");
        });

        modelBuilder.Entity<Class>(entity =>
        {
            entity.HasKey(e => e.Id).HasName("PK__Class__3213E83FE1AE3C4C");

            entity.ToTable("Class");

            entity.Property(e => e.Id).HasColumnName("id");
            entity.Property(e => e.ClassName)
                .HasMaxLength(100)
                .HasColumnName("className");
            entity.Property(e => e.Teacherid).HasColumnName("teacherid");

            entity.HasOne(d => d.Teacher).WithMany(p => p.Classes)
                .HasForeignKey(d => d.Teacherid)
                .OnDelete(DeleteBehavior.SetNull)
                .HasConstraintName("FK__Class__teacherid__39AD8A7F");
        });

        modelBuilder.Entity<ClassEvent>(entity =>
        {
            entity.HasKey(e => e.Id).HasName("PK__ClassEve__3213E83F2F109F3D");

            entity.ToTable("ClassEvent");

            entity.Property(e => e.Id).HasColumnName("id");
            entity.Property(e => e.Classid).HasColumnName("classid");
            entity.Property(e => e.Eventid).HasColumnName("eventid");

            entity.HasOne(d => d.Class).WithMany(p => p.ClassEvents)
                .HasForeignKey(d => d.Classid)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK__ClassEven__class__47FBA9D6");

            entity.HasOne(d => d.Event).WithMany(p => p.ClassEvents)
                .HasForeignKey(d => d.Eventid)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK__ClassEven__event__48EFCE0F");
        });

        modelBuilder.Entity<ClassRoster>(entity =>
        {
            entity.HasKey(e => e.Id).HasName("PK__ClassRos__3213E83FBB0C3F10");

            entity.ToTable("ClassRoster");

            entity.Property(e => e.Id).HasColumnName("id");
            entity.Property(e => e.Classid).HasColumnName("classid");
            entity.Property(e => e.Studentid).HasColumnName("studentid");

            entity.HasOne(d => d.Class).WithMany(p => p.ClassRosters)
                .HasForeignKey(d => d.Classid)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK__ClassRost__class__3C89F72A");

            entity.HasOne(d => d.Student).WithMany(p => p.ClassRosters)
                .HasForeignKey(d => d.Studentid)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK__ClassRost__stude__3D7E1B63");
        });

        modelBuilder.Entity<Event>(entity =>
        {
            entity.HasKey(e => e.Id).HasName("PK__Events__3213E83F31B8A794");

            entity.Property(e => e.Id).HasColumnName("id");
            entity.Property(e => e.EventDate).HasColumnName("eventDate");
            entity.Property(e => e.EventLocation)
                .HasMaxLength(255)
                .HasColumnName("eventLocation");
            entity.Property(e => e.EventName)
                .HasMaxLength(100)
                .HasColumnName("eventName");
            entity.Property(e => e.Geofenceid).HasColumnName("geofenceid");
            entity.Property(e => e.Status).HasColumnName("status");
            entity.Property(e => e.Teacherid).HasColumnName("teacherid");

            entity.HasOne(d => d.Geofence).WithMany(p => p.Events)
                .HasForeignKey(d => d.Geofenceid)
                .OnDelete(DeleteBehavior.SetNull)
                .HasConstraintName("FK__Events__geofence__451F3D2B");

            entity.HasOne(d => d.Teacher).WithMany(p => p.Events)
                .HasForeignKey(d => d.Teacherid)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK__Events__teacheri__442B18F2");
        });

        modelBuilder.Entity<GeoFence>(entity =>
        {
            entity.HasKey(e => e.Id).HasName("PK__GeoFence__3213E83F8D362DA0");

            entity.ToTable("GeoFence");

            entity.Property(e => e.Id).HasColumnName("id");
            entity.Property(e => e.EventRadius).HasColumnName("eventRadius");
            entity.Property(e => e.Latitude).HasColumnName("latitude");
            entity.Property(e => e.Longitude).HasColumnName("longitude");
            entity.Property(e => e.PairDistance).HasColumnName("pairDistance");
            entity.Property(e => e.TeacherRadius).HasColumnName("teacherRadius");
        });

        modelBuilder.Entity<Location>(entity =>
        {
            entity.HasKey(e => e.Id).HasName("PK__Location__3213E83FA6540D04");

            entity.Property(e => e.Id).HasColumnName("id");
            entity.Property(e => e.Latitude).HasColumnName("latitude");
            entity.Property(e => e.LocAccuracy).HasColumnName("locAccuracy");
            entity.Property(e => e.LocAltitude).HasColumnName("locAltitude");
            entity.Property(e => e.LocBearing).HasColumnName("locBearing");
            entity.Property(e => e.LocProvider)
                .HasMaxLength(150)
                .HasColumnName("locProvider");
            entity.Property(e => e.LocSpeed).HasColumnName("locSpeed");
            entity.Property(e => e.Longitude).HasColumnName("longitude");
            entity.Property(e => e.TimestampMs).HasColumnName("timestamp_ms");
            entity.Property(e => e.Userid).HasColumnName("userid");

            entity.HasOne(d => d.User).WithMany(p => p.Locations)
                .HasForeignKey(d => d.Userid)
                .HasConstraintName("FK__Locations__useri__52793849");
        });

        modelBuilder.Entity<Log>(entity =>
        {
            entity.HasKey(e => e.Id).HasName("PK__Logs__3213E83F2B32E7C9");

            entity.Property(e => e.Id).HasColumnName("id");
            entity.Property(e => e.Eventid).HasColumnName("eventid");
            entity.Property(e => e.LogDescription).HasColumnName("logDescription");
            entity.Property(e => e.Timestamp)
                .HasDefaultValueSql("(getdate())")
                .HasColumnType("datetime")
                .HasColumnName("timestamp");

            entity.HasOne(d => d.Event).WithMany(p => p.Logs)
                .HasForeignKey(d => d.Eventid)
                .OnDelete(DeleteBehavior.SetNull)
                .HasConstraintName("FK__Logs__eventid__62AFA012");
        });

        modelBuilder.Entity<LogsForActiveEventsView>(entity =>
        {
            entity
                .HasNoKey()
                .ToView("LogsForActiveEventsView");

            entity.Property(e => e.EventName)
                .HasMaxLength(100)
                .HasColumnName("eventName");
            entity.Property(e => e.Eventid).HasColumnName("eventid");
            entity.Property(e => e.LogDescription).HasColumnName("logDescription");
            entity.Property(e => e.Timestamp)
                .HasColumnType("datetime")
                .HasColumnName("timestamp");
        });

        modelBuilder.Entity<Message>(entity =>
        {
            entity.HasKey(e => e.Id).HasName("PK__Messages__3213E83F3FB5C8DD");

            entity.Property(e => e.Id).HasColumnName("id");
            entity.Property(e => e.MessageText).HasColumnName("messageText");
            entity.Property(e => e.Pairid).HasColumnName("pairid");
            entity.Property(e => e.Recipientid).HasColumnName("recipientid");
            entity.Property(e => e.Senderid).HasColumnName("senderid");
            entity.Property(e => e.SentAt)
                .HasDefaultValueSql("(getdate())")
                .HasColumnType("datetime")
                .HasColumnName("sent_at");

            entity.HasOne(d => d.Pair).WithMany(p => p.Messages)
                .HasForeignKey(d => d.Pairid)
                .OnDelete(DeleteBehavior.SetNull)
                .HasConstraintName("FK__Messages__pairid__5832119F");

            entity.HasOne(d => d.Recipient).WithMany(p => p.MessageRecipients)
                .HasForeignKey(d => d.Recipientid)
                .HasConstraintName("FK__Messages__recipi__573DED66");

            entity.HasOne(d => d.Sender).WithMany(p => p.MessageSenders)
                .HasForeignKey(d => d.Senderid)
                .HasConstraintName("FK__Messages__sender__5649C92D");
        });

        modelBuilder.Entity<MostRecentStudentLocationView>(entity =>
        {
            entity
                .HasNoKey()
                .ToView("MostRecentStudentLocationView");

            entity.Property(e => e.EventName)
                .HasMaxLength(100)
                .HasColumnName("eventName");
            entity.Property(e => e.Eventid).HasColumnName("eventid");
            entity.Property(e => e.FirstName)
                .HasMaxLength(100)
                .HasColumnName("firstName");
            entity.Property(e => e.LastName)
                .HasMaxLength(100)
                .HasColumnName("lastName");
            entity.Property(e => e.Latitude).HasColumnName("latitude");
            entity.Property(e => e.Longitude).HasColumnName("longitude");
            entity.Property(e => e.TimestampMs).HasColumnName("timestamp_ms");
        });

        modelBuilder.Entity<Notification>(entity =>
        {
            entity.HasKey(e => e.Id).HasName("PK__Notifica__3213E83FEBBCE99C");

            entity.Property(e => e.Id).HasColumnName("id");
            entity.Property(e => e.Eventid).HasColumnName("eventid");
            entity.Property(e => e.NotificationDescription).HasColumnName("notificationDescription");
            entity.Property(e => e.NotificationTimestamp)
                .HasDefaultValueSql("(getdate())")
                .HasColumnType("datetime")
                .HasColumnName("notificationTimestamp");
            entity.Property(e => e.Sent).HasColumnName("sent");
            entity.Property(e => e.Userid).HasColumnName("userid");

            entity.HasOne(d => d.Event).WithMany(p => p.Notifications)
                .HasForeignKey(d => d.Eventid)
                .OnDelete(DeleteBehavior.SetNull)
                .HasConstraintName("FK__Notificat__event__5EDF0F2E");

            entity.HasOne(d => d.User).WithMany(p => p.Notifications)
                .HasForeignKey(d => d.Userid)
                .OnDelete(DeleteBehavior.SetNull)
                .HasConstraintName("FK__Notificat__useri__5DEAEAF5");
        });

        modelBuilder.Entity<Pair>(entity =>
        {
            entity.HasKey(e => e.Id).HasName("PK__Pairs__3213E83F38ECF873");

            entity.Property(e => e.Id).HasColumnName("id");
            entity.Property(e => e.Eventid).HasColumnName("eventid");
            entity.Property(e => e.Status).HasColumnName("status");
            entity.Property(e => e.Student1id).HasColumnName("student1id");
            entity.Property(e => e.Student2id).HasColumnName("student2id");

            entity.HasOne(d => d.Event).WithMany(p => p.Pairs)
                .HasForeignKey(d => d.Eventid)
                .HasConstraintName("FK__Pairs__eventid__4F9CCB9E");

            entity.HasOne(d => d.Student1).WithMany(p => p.PairStudent1s)
                .HasForeignKey(d => d.Student1id)
                .HasConstraintName("FK__Pairs__student1i__4DB4832C");

            entity.HasOne(d => d.Student2).WithMany(p => p.PairStudent2s)
                .HasForeignKey(d => d.Student2id)
                .HasConstraintName("FK__Pairs__student2i__4EA8A765");
        });

        modelBuilder.Entity<StudentProfile>(entity =>
        {
            entity.HasKey(e => e.Id).HasName("PK__StudentP__3213E83F2FF20685");

            entity.ToTable("StudentProfile");

            entity.Property(e => e.Id)
                .ValueGeneratedNever()
                .HasColumnName("id");
            entity.Property(e => e.GradeLevel)
                .HasMaxLength(20)
                .HasColumnName("gradeLevel");

            entity.HasOne(d => d.IdNavigation).WithOne(p => p.StudentProfile)
                .HasForeignKey<StudentProfile>(d => d.Id)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK__StudentProfi__id__36D11DD4");
        });

        modelBuilder.Entity<StudentsInEventsView>(entity =>
        {
            entity
                .HasNoKey()
                .ToView("StudentsInEventsView");

            entity.Property(e => e.EventName)
                .HasMaxLength(100)
                .HasColumnName("eventName");
            entity.Property(e => e.FirstName)
                .HasMaxLength(100)
                .HasColumnName("firstName");
            entity.Property(e => e.LastName)
                .HasMaxLength(100)
                .HasColumnName("lastName");
        });

        modelBuilder.Entity<TeacherProfile>(entity =>
        {
            entity.HasKey(e => e.Id).HasName("PK__TeacherP__3213E83F09658447");

            entity.ToTable("TeacherProfile");

            entity.Property(e => e.Id)
                .ValueGeneratedNever()
                .HasColumnName("id");

            entity.HasOne(d => d.IdNavigation).WithOne(p => p.TeacherProfile)
                .HasForeignKey<TeacherProfile>(d => d.Id)
                .HasConstraintName("FK__TeacherProfi__id__33F4B129");
        });

        modelBuilder.Entity<User>(entity =>
        {
            entity.HasKey(e => e.Id).HasName("PK__Users__3213E83F46455F9F");

            entity.Property(e => e.Id).HasColumnName("id");
            entity.Property(e => e.FirstName)
                .HasMaxLength(100)
                .HasColumnName("firstName");
            entity.Property(e => e.LastName)
                .HasMaxLength(100)
                .HasColumnName("lastName");
            entity.Property(e => e.UserType)
                .HasMaxLength(20)
                .HasColumnName("userType");
        });

        OnModelCreatingPartial(modelBuilder);
    }

    partial void OnModelCreatingPartial(ModelBuilder modelBuilder);
}
