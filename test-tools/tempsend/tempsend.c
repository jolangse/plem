#include <ctype.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <netinet/in.h>
#include <sys/socket.h>

int
main (int argc, char **argv)
{
        int i; // Iterator
	int c; // Option character

	char *server = NULL; 	// IP Address as string, filled from -s option
	int sflag = 0; 		// Flag for valid server IP option

	char *pvalue = NULL; 	// Port number as string, filled from -p option
	int port; 		// Port number as int, converted from pvalue using strtol
	int pflag = 0; 		// Flag for valid port number option

	char *cvalue = NULL; 	// Temperature value as string, filled from -v option
	double v; 		// Value as double, converted from cvalue using strtod
	short value; 		// Value as int16
	int vflag = 0; 		// Flag for valid value

	char *node_id = NULL; 	// Sensor/node ID, filled from -i option, ten char's representing 5 bytes in hex
	int iflag = 0; 		// Flag for valid ID

	char buf[100]; 		// Buffer for data to be sendt to LEMP server
	int n; 			// String buffer position

        int sockfd;
        struct sockaddr_in servaddr;
        struct sockaddr_in cliaddr;

	opterr = 0;

	while ((c = getopt (argc, argv, "s:p:i:v:")) != -1)
		switch (c)
		{
			case 's':
				server = optarg;
				if ( inet_addr(server) != (in_addr_t)(-1))
					sflag = 1;
				break;
			case 'p':
				pvalue = optarg;
				if ( strtol(pvalue, NULL, 10) != 0 )
					pflag = 1;
				port = strtol(pvalue, NULL, 10);
				break;
			case 'i':
				node_id = optarg;
				if ( strlen(node_id) == 10 )
					iflag = 1;
				break;
			case 'v':
				cvalue = optarg;
				v = strtod(cvalue, NULL);
				value = (short)( v * 16.0 );	 
				vflag = 1;
				break;
			case '?':
				if ( (optopt == 'v') || (optopt == 'i')|| (optopt == 'p') || (optopt == 's') )
					fprintf (stderr, "Option -%c requires an argument.\n", optopt);
				else if (isprint (optopt))
					fprintf (stderr, "Unknown option `-%c'.\n", optopt);
				else
					fprintf (stderr,
							"Unknown option character `\\x%x'.\n",
							optopt);
				break;
			default:
				break;
		}

	// If any of the flags failed to be set, we have an options error.
	if ( ( !sflag) || (!pflag) || (!iflag)|| (!vflag) )
	{
		fprintf(stderr, " \n");
		fprintf(stderr, "Invalid options!\n");
		fprintf(stderr, " Valid, and required options are:\n");
		fprintf(stderr, "  \n");
		fprintf(stderr, "  -s <ip of LEMP server>\n");
		fprintf(stderr, "  \n");
		fprintf(stderr, "  -p <portnumber of LEMP server>\n");
		fprintf(stderr, "  \n");
		fprintf(stderr, "  -i <node id>\n");
		fprintf(stderr, "      Node ID is five bytes, written as hexadecimal digits.\n");
		fprintf(stderr, "      Software Node ID _should_ start with 01, and the last\n");
		fprintf(stderr, "      byte should be a sensor index. i.e. 01 for a single sensor system.\n");
		fprintf(stderr, "      Example of valid Node ID string: 0100FF0201\n");
		fprintf(stderr, "   \n");
		fprintf(stderr, "  -v <value as float>\n");
		fprintf(stderr, "      The value is parsed from string to double, and should be\n");
		fprintf(stderr, "      within the range 155.0 to -55.0\n");
		fprintf(stderr, " \n");
		return 1;
	}

        if ((sockfd=socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP))==-1)
        {
                perror("socket");
                return(1);
        }

        bzero(&servaddr,sizeof(servaddr));
        servaddr.sin_family = AF_INET;
        servaddr.sin_addr.s_addr=inet_addr(server);
        servaddr.sin_port=htons(port);

	// Building a LEMP1 packet. Start with the LEMP1 packet head
	// and the node identifier. Using Node ID type 1 here.
	n = 0;
	buf[n] = 'L'; n++;
	buf[n] = 'E'; n++;
	buf[n] = 'M'; n++;
	buf[n] = 'P'; n++;
	buf[n] = '1'; n++;
	buf[n] = 0x0; n++;
	buf[n] = 0x1; n++;
	buf[n] = 0x5; n++;
	for ( i=0; i < 5; i++)
	{
		int g; char h[3];
		strncpy(h, node_id+(2*i), 2);
		h[2] = 0;
		g = strtol(h, NULL, 16);
		buf[n] = (char)g;
		n++;
	}
	// Using a LEMP1 data type 8, temperature value as int16 representation
	// of temperature*16.
	buf[n] = 0x8; n++;
	buf[n] = 0x2; n++;
	buf[n] = (value>>8) & 0x00FF; n++;
	buf[n] = (value) & 0x00FF; n++;
	
        sendto(sockfd, buf, n, 0,
             (struct sockaddr *)&servaddr,sizeof(servaddr));

	return 0;
}
