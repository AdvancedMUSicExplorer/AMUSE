function [f_CENS,f_chroma,sideinfo] = main_STMSP_to_CENS(f_pitch,sideinfo)
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Name: main_STMSP_to_CENS.m
% Date of Revision: 15.12.2009
% Programmer: Meinard Mueller, Sebastian Ewert
% I/O modified for AMUSE: Igor Vatolkin, 17.05.2010
%
% Description: 
% * Computes CENS features (f_CENS) from pitch features (f_pitch) 
% * CENS is a chroma-like feature 
%   (Chroma Energy Normalized Statistics)
%
% Reference: 
% Details on the feature computation can be found in the following book:
%
% Meinard Mueller: Information Retrieval for Music and Motion,
%                  Springer 2007
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

close all hidden;

parameter.vis = 0;
parameter.stat_window_length  = 21;
parameter.stat_downsample     = 5;
parameter.featureRate = sideinfo.pitchSTMSP.featureRate;
parameter.save=0;
parameter.print = 0;

[f_CENS,f_chroma,sideinfo] = pitchSTMSP_to_CENS(f_pitch,parameter,sideinfo);
