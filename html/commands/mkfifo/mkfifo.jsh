#!/usr/local/bin/perl -w
# $Id: mkfifo.jsh,v 1.3 2004/08/05 14:20:35 cwest Exp $

use strict;
use POSIX "mkfifo";
use Getopt::Std;
use vars qw($opt_m);

$0 =~ s(.*/)();
my $usage = "usage: $0 [-m mode] filename ...\n";
getopts('m:') and @ARGV or die $usage;

my $default_mode = 0666;
$default_mode &= ~(umask 0);

sub sym_perms {
	my $sym = shift;
	my $mode = $default_mode;

	my %who = (u => 0700, g => 0070, o => 0007);
	my %what = (r => 0444, w => 0222, x => 0111);

	my ($who, $how, $what) = split /([+-=])/, $sym;
	$who =~ s/a/ugo/g;

	my @who = split //, $who;
	my $who_mask = 0;
	foreach (@who) {
		$who_mask |= $who{$_};
	}

	my @what = split //, $what;
	my $what_mask = 0;
	foreach (@what) {
		$what_mask |= $what{$_};
	}

#	printf "%o, %o, %o\n", $who_mask, $what_mask, $change;
#	print "$how\n";

	if ($how eq '+') {
		$mode |= ($who_mask & $what_mask);
	} elsif ($how eq '-') {
		$mode &= ~($who_mask & $what_mask);
	} elsif ($how eq '=') {
		$mode = ($mode & ~$who_mask) | ($who_mask & $what_mask);
	}
}

sub get_mode {
	my $mode = shift;
	my $real_mode;

	if ($mode =~ /^0?[0-7]{3}$/) {
		return $real_mode = oct($mode);
	}
	$real_mode = sym_perms $mode;
	return $real_mode unless $real_mode < 0;
	die "bad mode: $mode\n";
}

my $mode = $opt_m ? get_mode $opt_m : $default_mode;

foreach my $fifo (@ARGV) {
	mkfifo $fifo, $mode or die "can't make fifo $fifo: $!\n";
}


=head1 NAME

mkfifo - make named pipes

=head1 SYNOPSIS

mkfifo "-m mode" filename ...

=head1 DESCRIPTION

=over 2

Create one or more named pipes, in the order specified,
with the mode given.

If no mode is given, create them with mode 0666, modified by the umask.

=back

=head1 OPTIONS AND ARGUMENTS

=over 8

=item I<-m>

The mode the fifo should be created with.

Numbers must be three octal digits (as for B<chmod(1)>.

Symbolic modes, specified the way you can for B<chmod(1)>
(such as C<g+w>) are also acceptable.

=item I<filename ...>

One or more fifo names to create

=back

=head1 AUTHOR

Jeffrey S. Haemer and Louis Krupp

=head1 SEE ALSO

  chmod(1) umask(1) mkfifo(2)

=cut