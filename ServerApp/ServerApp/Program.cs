using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ServerApp
{
    class Program
    {
        static void Main(string[] args)
        {
            RadioServer server = new RadioServer("127.0.0.1", 7000);
            server.Run();
        }
    }
}
